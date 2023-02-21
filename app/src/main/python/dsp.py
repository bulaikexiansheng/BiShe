# 数字信号处理

import numpy as np
import scipy
from scipy.signal import savgol_filter
from scipy import signal
from sklearn import preprocessing

def z_score(data):
    zscore = preprocessing.StandardScaler()
    data_zs = zscore.fit_transform(data)
    return data_zs

def rms(data):
    return np.mean((data*data))**0.5


def energy(data, kernel_size, stride):
    out_len = int((len(data)-kernel_size)/stride)+1
    energy_data = np.zeros(out_len)
    for i in range(out_len):
        temp = data[i*stride:i*stride+kernel_size]
        energy_data[i] = np.mean(abs(temp*temp))
        # energy_data[i] = sum(abs(temp * temp))
    return energy_data


def smooth(data, window_length, polyorder):
    return savgol_filter(data, window_length, polyorder)


def highpass(data, order, f, fs):
    b, a = signal.butter(order, 2 * f / fs, 'highpass')
    return signal.filtfilt(b, a, data)


def lowpass(data, order, f, fs):
    b, a = signal.butter(order, 2 * f / fs, 'lowpass')  # 配置滤波器，8表示滤波器的阶数
    return signal.filtfilt(b, a, data)


def bpf_fft(signal, fs, fc1, fc2):
    length = len(signal)
    k1 = int(fc1*length/fs)
    k2 = int(fc2*length/fs)
    signal_fft = scipy.fftpack.fft(signal)
    signal_fft[0:k1] = 0+0j
    signal_fft[k2:length-k2] = 0+0j
    signal_fft[length-k1:length] = 0+0j
    signal_ifft = scipy.fftpack.ifft(signal_fft)
    result = signal_ifft.real
    return result


def bpf_fir(signal,fs,fc1,fc2,numtaps=101):
    b=scipy.signal.firwin(numtaps, [fc1, fc2], pass_zero=False, fs=fs)
    result = scipy.signal.lfilter(b, 1, signal)
    return result


def cleanoffset(signal):
    avg=np.mean(signal)
    signal=signal-avg
    return signal


def OutlierDetection(signal):
    z_score = (signal - np.mean(signal)) / (np.std(signal)+0.001)
    for i in range(len(signal)):
        if np.abs(z_score[i]) > 2.5:
            signal[i] = np.median(signal)
    return signal




def SNR(data, reference, kernel_size, stride):
    out_len = int((len(data)+1-kernel_size)/stride)
    snr = np.zeros(out_len)
    for i in range(out_len):
        snr[i] = rms(data[i*stride:i*stride+kernel_size]) / rms(reference)
    return snr


# 插值
def interp(y, length, usescipy=False):
    x_old = np.linspace(0, len(y) - 1, num=len(y))
    x_new = np.linspace(0, len(y) - 1, num=length)
    if usescipy:
        f = scipy.interpolate.interp1d(x_old, y, kind='cubic')
        result = f(x_new)
    else:
        result = np.interp(x_new, x_old, y)

    return result
