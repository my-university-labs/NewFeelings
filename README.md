### New Feelings

Android平台相册应用，使用[Google开源机器学习框架tensorflow](https://github.com/tensorflow/tensorflow)处理图片以提供更好的图片浏览体验

created by DongchangZhang @ 20170119

> 作者注：
>
> 1. 该repository是我们小组项目[Album-Category](https://github.com/gaohuangzhang/Album-Category)项目的第二轮迭代，由于其他成员不再参加开发，[Album-Category](https://github.com/gaohuangzhang/Album-Category)不再进行后续的更新以及相应的问题修复，项目迁移到当前仓库。
> 2. 该repository将尽可能重构一期项目的代码并修复一期项目中存在的bug，并且添加新的特性和功能。
> 3. 本项目与[Album-Category](https://github.com/gaohuangzhang/Album-Category)的区别除了功能的完善和更新之外，没有添加[Galahad-moye](https://github.com/Galahad-moye)所完成的语音搜索功能。
> 4. 因为种种原因，创建该repository时放弃了[Album-Category](https://github.com/gaohuangzhang/Album-Category) Git提交历史以及其他相应的信息，可以到这里查看[开发历史](https://github.com/gaohuangzhang/Album-Category) 。

#### 一期项目功能及其缺陷

[试用一期成果APK](https://github.com/gaohuangzhang/Album-Category/blob/master/app/app-release.apk)

功能：浏览本机图片，使用tf处理图片并分类，每一个相册的幻灯片放映，拍照并且处理图片。

缺陷：图片太多第一次进入app将花费太多时间等待处理。图片太多浏览图片滑动卡顿。幻灯片放映不能选择音乐，不能选择图片。不能自定义相册。浏览图片方式单调。android6.0的运行时权限问题没有解决。拍照时会保存两张相同的照片。

#### 二期项目功能更新

* update 1@ 20170119

  重命名包，解决运行时权限问题，重构数据库操作代码并去除多余的重复代码，处理图片的方式多样化：由用户设置是进入app处理、全部后台处理、或者机器智能处理，增加对x86机器的支持。