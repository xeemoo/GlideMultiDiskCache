
#keep原因: Glide处理自定义缓存需要反射获取以下两个类中属性
-keep class com.bumptech.glide.load.engine.ResourceCacheKey
-keep class com.bumptech.glide.load.engine.DataCacheKey