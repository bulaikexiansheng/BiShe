import numpy
import threading
import pandas as pd
import numpy as np
import re
import torch
import time
import os
from queue import Queue
import dsp
from main import Deployment
from process import delete_prefix, outlier_detection
from datastruct import DataList
from concurrent.futures import ThreadPoolExecutor

# 线程池
thread_pool_for_segment = ThreadPoolExecutor(max_workers=1)
thread_pool_for_predict = ThreadPoolExecutor(max_workers=1)

# 任务预测队列
predictTaskQueue = Queue(maxsize=1)

# 结果队列
resultQueue = Queue(maxsize=1)

# 数据缓冲区
dataList = DataList()


def getResult():
    """
    Java端通过获得结果
    :param self:
    :return:
    """
    return resultQueue.get()


class MainThread(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        # 模型加载
        self.model = torch.load(os.path.join(os.path.dirname(__file__), 'model.pt'), map_location='cpu')
        self.stride = 10

    def run(self):
        self.submitNetworkForResult()

    def analyzeMotion(self, headRate, bodyRate):
        print("headRate: %s bodyRate: %s" % (headRate, bodyRate))
        """
        分析模型结果
        :param motionRate: 头部运动的概率数组
        :param bodyRate:   身体姿态的概率数组
        :return: 返回头部运动、身体姿态
        """
        head_dict = {0: "up", 1: "down", 2: "left", 3: "right", 4: "lean_left", 5: "lean_right", 6: "up&up",
                     7: "down&down", 8: "left&left", 9: "right&right", 10: "left&right", 11: "right&left"}
        body_dict = {0: "downstairs", 1: "upstairs", 2: "still", 3: "walk", 4: "jog"}
        maxHeadRate = headRate[0]
        maxBodyRate = bodyRate[0]
        body = body_dict.get(0)
        head = head_dict.get(0)
        body_index = 0
        head_index = 0
        for i in range(headRate.shape[0]):
            if headRate[i] > maxHeadRate:
                maxHeadRate = headRate[i]
                head = head_dict.get(i)
                head_index = i
        print("maxHeadRate : %f maxHeadIndex : %d" % (maxHeadRate, head_index))
        for i in range(bodyRate.shape[0]):
            if bodyRate[i] > maxBodyRate:
                maxBodyRate = bodyRate[i]
                body = body_dict.get(i)
                body_index = i
        print("maxBodyRate : %f maxBodyIndex : %d" % (maxBodyRate, body_index))

        return head_index, body_index, head, body

    def submitNetworkForResult(self):
        """
        提交到网络进行预测
        :return:
        data = []
        for i in ["Gx","GY","GZ","AX","AY","AZ"]:
            data.append([i + str(k) for k in range(1,301)])
        """
        data = predictTaskQueue.get()  # 6 * 300 的 list
        print(
            "submitNetWorkForResult : r %d c %d %d %d %d %d %d " % (len(data), len(data[0]), len(data[1]), len(data[2]),
                                                                    len(data[3]), len(data[4]), len(data[5])))
        data = np.array(data).astype(float)
        data = np.transpose(data)
        final = data[:50, :]
        for i in range(10, 260, 10):
            final = np.append(final, data[i:i + 50, :], axis=0)
        final = final.reshape(26, 50, 6)
        final = torch.tensor(final, dtype=torch.float)  # list转换为张量
        final = torch.unsqueeze(final, dim=0)
        start_time = time.time()
        with torch.no_grad():
            result = self.model(final)
        end_time = time.time()
        t = (end_time - start_time) * 1000
        print("cost time %f ms" % t)
        head = result[0][0]  # 动作
        body = result[1][0]  # 身体姿态
        head, body, headDes, bodyDes = self.analyzeMotion(head, body)
        print("head body : [ %s, %s ]" % (headDes, bodyDes))
        resultQueue.put([head, body, end_time - start_time])


class PredictThread(threading.Thread):
    """
    模拟蓝牙传输数据
    """

    def __init__(self):
        """
        rawQueue蓝牙传输过来的原生数据保存在这个阻塞队列中
        :param path:
        """
        threading.Thread.__init__(self)
        self.rawDataQueue = Queue(maxsize=1)
        self.count = 0
        self.last_string = ""

    def predict(self, raw_data):
        """
        java端调用该函数，传输蓝牙原生数据
        :param raw_data: 蓝牙原生数据
        :return:
        """
        # 数据传输到这里后，直接进入缓存
        print(raw_data)
        self.save_In_Cached(raw_data)
        # 数据接收次数
        self.count += 1

    def save_In_Cached(self, raw_string):
        """
        数据截取，AXxxxDAYxxxD --> AXxxx and AYxxx,放入数据队列中等待处理
        """
        string = self.last_string + raw_string
        result = string.split("D")
        self.last_string = result[len(result) - 1]
        self.rawDataQueue.put(result[:len(result) - 1])
        # if self.last_string != "D":
        #     print("last_string: %s" % self.last_string)

    def inputVector(self, flag, number):
        """
        将数据输入到不同数据队列中
        :param flag:
        :param number:
        :return:
        """
        dataList.push_data(flag, number)

    def run(self):
        while True:
            # 从队列中取得蓝牙发送的数据，队列空的时候会阻塞
            raw_data = self.rawDataQueue.get()
            # # [AX3.00,...] -> [3.00,...]
            # # 维度分类，AX-->AXQueue AY-->AYQueue
            print(raw_data)
            for i in raw_data:
                l = re.split("[a-zA-Z:\\s]", i)
                numStr = l[len(l) - 1]
                self.inputVector(i[:2], numStr)

            # 分割
            segmentThread = SegmentThread(dataList)
            thread_pool_for_segment.submit(segmentThread.run())
            segmentThread.start()


def shape(mList, name):
    """
    用来debug的函数
    :param mList:
    :param name:
    :return:
    """
    print("%s shape:(%d,%d)" % (name, len(mList), len(mList[0])))


def here():
    print("I am here!!!!!!!!")


def printVal(value, name):
    print("%s : %s" % (name, value))


class SegmentThread(threading.Thread):
    """
    切分算法
    """
    # 每满10传入predict
    segment_size = 10
    # 6维
    dimension = 6

    # 预测模型
    th_end = 4
    th_start = 10
    deployment = Deployment(th_end=th_end, th_start=th_start)
    segment_start = 0

    def __init__(self, mList):
        threading.Thread.__init__(self)
        self.data_list = mList

    def isLegal(self):
        return SegmentThread.segment_start < self.data_list.getSize("GX") - SegmentThread.segment_size and \
            SegmentThread.segment_start < self.data_list.getSize("GY") - SegmentThread.segment_size and \
            SegmentThread.segment_start < self.data_list.getSize("GZ") - SegmentThread.segment_size and \
            SegmentThread.segment_start < self.data_list.getSize("AX") - SegmentThread.segment_size and \
            SegmentThread.segment_start < self.data_list.getSize("AY") - SegmentThread.segment_size and \
            SegmentThread.segment_start < self.data_list.getSize("AZ") - SegmentThread.segment_size

    def run(self):
        # 取 6 * 10 数据
        segment_data = []
        # 每10个数据传进Deployment及逆行动作切割
        if self.isLegal():
            temp = SegmentThread.segment_size + SegmentThread.segment_start
            segment_data.append(self.data_list.GXList[SegmentThread.segment_start: temp])
            segment_data.append(self.data_list.GYList[SegmentThread.segment_start: temp])
            segment_data.append(self.data_list.GZList[SegmentThread.segment_start: temp])
            segment_data.append(self.data_list.AXList[SegmentThread.segment_start: temp])
            segment_data.append(self.data_list.AYList[SegmentThread.segment_start: temp])
            segment_data.append(self.data_list.AZList[SegmentThread.segment_start: temp])

            SegmentThread.segment_start += SegmentThread.segment_size

            # 转换为float数据
            segment_data = np.array(segment_data)
            try:
                segment_data = segment_data.astype(float)
            except ValueError:
                print("segment_data: %s" % segment_data)
                for i in range(segment_data.shape[0]):
                    for j in range(segment_data.shape[1]):
                        firstDot = segment_data[i][j].find(".")
                        secondDot = segment_data[i][j].rfind(".")
                        if firstDot != secondDot:
                            segment_data[i][j] = segment_data[i][j][:secondDot]
                segment_data = segment_data.astype(float)
                print("执行修正程序")

            segment_data = outlier_detection(segment_data)
            # 滤波
            # for j in range(len(segment_data)):
            #     segment_data[j, :] = dsp.lowpass(segment_data[j, :], order=3, f=3, fs=100)
            # 动作切分
            flag, start, end = self.deployment.predict(segment_data)

            if flag:
                print("有动作！！")
                end = end + SegmentThread.segment_start - self.deployment.window1_size
                start = end - self.deployment.window1_size + start
                self.deployment.__init__(th_end=self.th_end, th_start=self.th_start)
                midPoint = int((start + end) / 2)
                # 6 * 300
                s = midPoint - 150
                e = midPoint + 150
                if s < 0:
                    s = 0
                    e = s + 300
                m = min([len(dataList.GXList), len(dataList.GYList), len(dataList.GZList),
                         len(dataList.AXList), len(dataList.AYList), len(dataList.AZList)])
                if e > m:
                    e = m
                    s = e - 300
                predicted_data = [
                    self.data_list.GXList[s:e],
                    self.data_list.GYList[s:e],
                    self.data_list.GZList[s:e],
                    self.data_list.AXList[s:e],
                    self.data_list.AYList[s:e],
                    self.data_list.AZList[s:e]
                ]
                # print("predicted_data: %s " % predicted_data)
                # toString(predicted_data)
                # getPicture(predicted_data)

                predictTaskQueue.put(predicted_data)  # 数据上传队列
                # 开始预测
                mainThread = MainThread()
                mainThread.start()
                self.data_list.clear_data(end)
                SegmentThread.segment_start = 0
            else:
                if SegmentThread.segment_start >= 800:
                    self.data_list.clear_data(400)
                    SegmentThread.segment_start -= 400


def toString(data):
    result = ""
    for i in range(len(data)):
        temp = ""
        for j in data[i]:
            temp = temp + j
            temp = temp + "、"
        temp = temp + "\n"
        result = result + temp
    print(result)
