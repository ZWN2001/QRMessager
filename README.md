# QRMessager
使用二维码在移动端免连接传送文件

缘起：灵感来源于扫家里WiFi二维码进行连接WiFi的时候头脑中的一个小火花

针对的问题：不同机型的手机之间如何在不进行蓝牙连接且不依赖网络的情况下进行信息与文件传输

其实也可以占一个WiFi频道进行局域网构建，但是吧。。。。不太值得，尤其是在传输小文件的场景下，构建连接所花费的成本远高于文件传输的成本。

文档后期会发布在个人博客进行一些代码的解析

基于HMS Core：

- [统一扫码服务](https://developer.huawei.com/consumer/cn/hms/huawei-scankit)
- [UI-kit](https://developer.huawei.com/consumer/cn/huawei-ui-kit/)

后期开发目标：

- 压缩传输
- 更高效的可视化码方案，降低编码冗余
- 更高效的扫描方案，感觉统一扫码服务为了服务的全面性牺牲了一些性能。
- 适配安卓10对文件的限制
