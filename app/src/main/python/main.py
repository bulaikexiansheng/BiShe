import numpy as np
import dsp
import torch


# 该函数用于测试txt的demo
def segment(data):
    th_end = 4
    th_start = 10
    start, end = 0, 0
    deployment = Deployment(th_end=th_end, th_start=th_start)
    for j in range(len(data)):
        data[j, :] = dsp.lowpass(data[j, :], order=3, f=3, fs=100)  # 做某种变化
    for j in range(0, data.shape[1], 10):  # 每10个数据丢进去一次，进行预测
        flag, start, end = deployment.predict(data[:, j:j + 10])
        if flag:
            end = end + j - deployment.window1_size
            start = end - deployment.window1_size + start
            deployment.__init__(th_end=th_end, th_start=th_start)
            return start, end
        else:
            start, end = 0, 0
    return start, end


class Deployment:
    def __init__(self, th_end, th_start):
        self.window_stride = 10  # 3个滑动窗口的步长
        self.window1_size = 400  # 动作结束点检测的滑动窗口的大小（略大于所有动作）
        self.window2_size = 50  # 上下文信息的滑动窗口的大小
        self.window3_size = 50  # 动作起始点检测的活动窗口的大小
        self.window4_size = int(self.window1_size / self.window_stride)  # 滑动窗口1对应的能量滑动窗口
        self.channel = 3  # 通道数，使用角速度
        self.end_threshold = th_end  # 判断滑动窗口1中信号的能量保持不变的阈值
        self.snr_threshold = th_start  # 起始点snr阈值
        self.flag = 0  # 当检测到结束点时，flag=1，进一步检测到起始点时，flag=2
        self.count = 0  # 起始阶段或刚检测完一个动作时，需要等待40个count后才能检测下一个动作，因为40*10=400
        self.end = 0  # 结束点
        self.start = 0  # 起始点
        self.context = []  # 滑动窗口2，保存上下文信息
        self.best_ch = self.channel + 1
        self.signal = np.zeros((self.channel, self.window1_size))  # 滑动窗口1
        self.window4 = np.zeros((self.channel, self.window4_size))  # 滑动窗口4
        # self.model = torch.load("Model//model.pt",map_location="cpu")

    def preprocess(self, sequence):
        self.signal = np.concatenate((self.signal, sequence[:3]), axis=1)
        if self.count <= self.window1_size / 10:
            self.context = self.signal[:, :self.window2_size]
        self.signal = self.signal[:, self.window_stride:]
        self.best_channel()
        energy = np.zeros((self.channel, 1))
        for i in range(self.channel):
            energy[i, 0] = np.mean(self.signal[i, :] * self.signal[i, :])
        self.window4 = np.delete(self.window4, 0, axis=1)
        self.window4 = np.column_stack((self.window4, energy))

    def best_channel(self):
        energy = []
        for i in range(self.channel):
            temp = np.mean(self.signal[i, :] * self.signal[i, :])  # 平均数
            energy.append(temp)
        self.best_ch = np.argmax(energy)

    def end_detection(self):
        interp_energy = dsp.interp(self.window4[self.best_ch, :], self.window1_size)
        smooth_energy = dsp.smooth(interp_energy, window_length=51, polyorder=1)
        energy_diff = np.diff(smooth_energy)
        energy_diff = dsp.interp(energy_diff, self.window1_size)

        for j in range(len(energy_diff)):
            if smooth_energy[j] > 0.8 * max(smooth_energy) and sum(energy_diff[j - 10:j]) < self.end_threshold:
                self.end = j
                self.flag = 1
                break
            else:
                self.flag = 0

    def start_detection(self):
        if self.flag == 1:
            context_energy = np.average(self.context[self.best_ch] * self.context[self.best_ch])
            window3 = dsp.energy(self.signal[self.best_ch, :], self.window3_size, self.window_stride)
            window3 = dsp.interp(window3, (len(window3) - 1) * 10)
            window3 = dsp.smooth(window3, window_length=21, polyorder=1)
            for i in range(len(window3)):
                if window3[i] > self.snr_threshold * context_energy:
                    self.start = i + (self.window1_size - len(window3))
                    self.flag = 2
                    break

    def predict(self, sequence):
        self.count += 1
        self.preprocess(sequence)
        if self.count > self.window1_size / 10:  # 滑动窗口大小被充满的时候再进行动作检测
            self.end_detection()  # 确定动作的起点和终点
            self.start_detection()
            min_len = 50
            # min_len = 80
            max_len = self.window1_size
            if self.flag == 2 and (self.end - self.start) > min_len and (self.end - self.end) < max_len:  # 检测出动作
                # TODO 输入模型，得出分类结果
                motion = True
            else:  # 未检测出动作
                motion = False
        else:
            motion = False
        self.flag = 0

        # 留裕量
        return motion, self.start - 0, self.end + 0
