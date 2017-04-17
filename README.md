WeChat Moment Export
--------------------
Deprecated. Try [WeChatMomentStat-Android](https://github.com/Chion82/WeChatMomentStat-Android) instead!

本项目已不再维护。请移步至无需依赖Xposed的[WeChatMomentStat-Android](https://github.com/Chion82/WeChatMomentStat-Android)

WeChatModuleExport is an Xposed module which helps you export WeChat moment data to JSON files.

本工具为Xposed模块，用于导出微信朋友圈数据。  
支持导出朋友圈文字内容、图片、段视频、点赞、评论等数据。

Supported WeChat Versions（目前支持的微信版本）:

[WeChat 6.3.13](https://github.com/Chion82/WeChatMomentExport/raw/master/weixin6313android740.apk)

<img src="https://raw.githubusercontent.com/Chion82/WeChatMomentExport/master/demo_1.jpg"  width="400px" >

Download
--------

[WeChatMomentExport Releases](https://github.com/Chion82/WeChatMomentExport/releases)  

Change Log
----------

* 2016-2-6 支持微信版本6.3.13.64_r4488992，修复抓取数据不齐的问题。因为需要清空微信朋友圈缓存，本APP将需要获得root权限。

* 2016-2-4 支持微信版本6.3.13，支持朋友圈图片视频等完整数据的导出，逆向APK时成功分析朋友圈数据模型，增加GUI配置面板

Usage
-----

## EN

* Make sure your Android device is rooted with Xposed Framework installed.

* Install and enable this Xposed module on your device. Reboot.

* Grant root access for the app.

* In the GUI preference panel, click "START INTERCEPT".

* Open WeChat and enter the Moments page.

* Scroll down to load more moments. Meanwhile the module should be capturing the loaded data automatically.

* Click "STOP INTERCEPT" when done.

* By default, the generated JSON file is ```moments_output.json``` located in the root directory of the external storage.

## ZH

* 确认安卓设备已root并安装好Xposed框架。

* 安装并启用本Xposed模块，然后重启手机。

* 允许本app获得root权限。

* 打开本模块的GUI设置页并点击“开始抓取”。

* 打开微信并进入朋友圈页面。

* 向下滚动页面来加载更多朋友圈。同时本插件会自动抓取已加载的朋友圈数据。

* 完成后，点“停止抓取”。

* 默认情况下，抓取并经过转换的JSON数据存放在外部存储根目录下的 ```moments_output.json``` 文件中。

* 如需抓取指定某个好友的朋友圈数据，在“开始抓取”之后直接从微信联系人进入TA的个人相册即可，在停止抓取之前不要打开朋友圈或者其他人的相册。

Known Issues
------------

* 个人相册中无法抓取纯文本朋友圈。

Build Requirements
------------------

* IDE: Android Studio

License
-------

GPLv3

Author
------

[Chion Tang的归宅开发部活动记录](https://blog.chionlab.moe)
