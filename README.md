#WeChat Moment Export

WeChatModuleExport is an Xposed module which helps you export WeChat moment data to JSON files. Only support WeChat version 6.3.8

本工具为Xposed模块，用于导出微信朋友圈数据。（目前仅可导出朋友圈文本、评论和点赞数据，仅支持微信版本6.3.8）

##Download

[0.1 beta](https://github.com/Chion82/WeChatMomentExport/releases/download/0.1beta/app-release.apk)  
[WeChat 6.3.8](https://github.com/Chion82/WeChatMomentExport/raw/master/weixin638android680.apk)

#Usage

##EN

* Make sure your Android device is rooted with Xposed Framework installed.

* Install and enable this Xposed module on your device. Reboot.

* Enter WeChat and open the Moments page.

* **Slightly** scroll down to load more moments. Meanwhile the module should be capturing the loaded data automatically.

* The exported JSON file is ```moments_export.json``` located in the root directory of the external storage.

##CN

* 确认安卓设备已root并安装好Xposed框架。

* 安装并启用本Xposed模块，然后重启手机。

* 进入微信并打开朋友圈页面。

* **缓慢地** 向下滚动页面来加载更多朋友圈。同时本插件会自动抓取已加载的朋友圈数据。

* 抓取并经过转换的JSON数据存放在外部存储根目录下的 ```moments_export.json``` 文件中。

#Build Requirements

* IDE: Android Studio

#License

MIT
