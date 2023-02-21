class DataList():
    """
    缓冲区队列
    """

    def __init__(self):
        self.GXList = []
        self.GYList = []
        self.GZList = []
        self.AXList = []
        self.AYList = []
        self.AZList = []

    def get_data(self, flag, index):
        """
        获得列表中的数据
        :param flag: 获得哪一个维度的数据
        :param index: 第几个数据
        :return: 返回取得的数据
        """
        result = 0
        if flag == "GX":
            result = self.GXList[index]
        elif flag == "GY":
            result = self.GYList[index]
        elif flag == "GZ":
            result = self.GZList[index]
        elif flag == "AX":
            result = self.AXList[index]
        elif flag == "AY":
            result = self.AYList[index]
        elif flag == "AZ":
            result = self.AZList[index]
        return result

    def push_data(self, flag, data):
        """
        缓存蓝牙传输过来的数据
        :param flag: GX、GY、GZ、AX、AY、AZ
        :param data: 数据本身
        """
        if flag == "GX":
            self.GXList.append(data)
        elif flag == "GY":
            self.GYList.append(data)
        elif flag == "GZ":
            self.GZList.append(data)
        elif flag == "AX":
            self.AXList.append(data)
        elif flag == "AY":
            self.AYList.append(data)
        elif flag == "AZ":
            self.AZList.append(data)
        else:
            return

    # 测试是否正确缓存数据
    def display_list_size(self):
        print("缓存队列大小: %s" % {"GX": len(self.GXList), "GY": len(self.GYList),
                                    "GZ": len(self.GZList), "AX": len(self.AXList),
                                    "AY": len(self.AYList), "AZ": len(self.AZXList)})

    def clear_data(self,end):
        """
        删除缓存的数据
        :param end: end之前的数据被清空
        :return: 无返回值
        """
        self.GXList = self.GXList[end:]
        self.GYList = self.GYList[end:]
        self.GZList = self.GZList[end:]
        self.AXList = self.AXList[end:]
        self.AYList = self.AYList[end:]
        self.AZList = self.AZList[end:]

    def getSize(self, flag):
        """
        得到size
        :return:
        """
        if flag == "GX":
            return len(self.GXList)
        elif flag == "GY":
            return len(self.GYList)
        elif flag == "GZ":
            return len(self.GZList)
        elif flag == "AX":
            return len(self.AXList)
        elif flag == "AY":
            return len(self.AYList)
        elif flag == "AZ":
            return len(self.AZList)