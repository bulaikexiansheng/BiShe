import numpy as np
import pandas as pd
import re


def delete_prefix(data):
    for i in range(len(data)):
        data[i] = data[i][2:]
    return data


def outlier_detection(data):
    for i in range(len(data)):
        data_series = data[i, :]
        rule = (data_series.mean() - 9 * data_series.std() > data_series) | \
               (data_series.mean() + 9 * data_series.std() < data_series)
        index = np.arange(data_series.shape[0])[rule]
        data = np.delete(data, index, axis=1)
    return data

# 将数据从txt文件中提取出来，转换为
def clean_data(path):
    raw_data = pd.read_table(filepath_or_buffer=path, sep='\t', header=None)
    raw_data = np.array(raw_data).reshape(-1)
    line = ''.join(raw_data)

    expression = [
        '(?:GX|GX-)\d+\.?\d*', '(?:GY|GY-)\d+\.?\d*', '(?:GZ|GZ-)\d+\.?\d*',
        '(?:AX|AX-)\d+\.?\d*', '(?:AY|AY-)\d+\.?\d*', '(?:AZ|AZ-)\d+\.?\d*',
        '(?:NX|NX-)\d+\.?\d*', '(?:NY|NY-)\d+\.?\d*', '(?:NZ|NZ-)\d+\.?\d*',
        '(?:QW|QW-)\d+\.?\d*', '(?:QX|QX-)\d+\.?\d*', '(?:QY|QY-)\d+\.?\d*', '(?:QZ|QZ-)\d+\.?\d*',
        '(?:BX|BX-)\d+\.?\d*', '(?:BY|BY-)\d+\.?\d*', '(?:BZ|BZ-)\d+\.?\d*'
    ]
    ch_idx = {'gyro': [0, 1, 2], 'acc': [3, 4, 5], 'mag': [6, 7, 8], 'qua': [9, 10, 11, 12], 'ang': [13, 14, 15]}

    data = []
    for i in range(len(expression)):
        data.append(delete_prefix(re.compile(expression[i]).findall(line)))
    # 划分仅仅用到Gyro
    delete_ch = ch_idx['mag'] + ch_idx['qua'] + ch_idx['ang']
    delete_ch.sort(reverse=True)
    for c in delete_ch:
        data.pop(c)

    data = np.array(data).astype(float)

    data = outlier_detection(data)

    return data

#
# print("1.txt")
# data = clean_data("data//1.txt")
# print(data.shape)
#
# print("1.txt")
# data = clean_data("data//1.txt")
# print(data.shape)
#
# print("2.txt")
# data = clean_data("data//2.txt")
# print(data.shape)
#
# print("3.txt")0000
# data = clean_data("data//3.txt")
# print(data.shape)
