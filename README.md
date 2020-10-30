# ZoomImageView

A custom ImageView Implementation

Supports
- Double-tap to zoom
- Pinch zoom
- Drag to pan
- Fling to pan
- Swipe to dismiss

![Double tap](media/double_tap.gif)
&nbsp;&nbsp;
![Pinch to zoom](media/pinch.gif)
&nbsp;&nbsp;  
![Pan](media/pan.gif)
&nbsp;&nbsp;
![Swipe to dismiss](media/swipe_dismiss.gif)

## Implementation

### Extends the standard `AppCompatImageView` api with a few minor additions

#### Add to layout
```xml
<com.k2.zoomimageview.ZoomImageView
    android:id="@+id/iv"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```
#### Drawable callback
```kotlin
imageView.onDrawableLoaded {
    // invoked when any image is loaded, like from a library like Glide, picasso or coil
    // can be used for updating progress UI
}
```
#### Get/Set zoom level
```kotlin
imageView.currentZoom = 1.5F // sets current zoom to 1.5 x
imageView.currentZoom // returns 1.5F
```
#### Reset pan and zoom
```kotlin
imageView.resetZoom()
```
#### Parent layout touch intercept
Allow/disallow parent viewgroup to intercept touch event while zoomed in. 
Panning and swiping in a parent like ViewPager can cause some issues with gesture detection
```kotlin
imageView.disallowPagingWhenZoomed = true
```
#### Debug information
Display drawable bounds and scale/translate info on the view.
Can be useful when modifying/debugging the view
```kotlin
imageView.debugInfoVisible = true
```
#### Swipe to dismiss

Enable/Disable
```kotlin
imageView.swipeToDismissEnabled = true
```
Dismiss callback. For best results use shared activity/fragment transition
```kotlin
imageView.onDismiss = {
    finishAfterTransition() or finish()
}
```
Track swipe to dismiss progress. Can be used to update UI accordingly
```kotlin
dismissProgressListener = { progress ->
    bgView.alpha = 1.0f - progress
}
```
## Planned improvements and future additions

- Support for multiple scale types (only fit-center works for now)
- Publish as a library (maybe)

## Credits
Huge thanks to [Chris Banes](https://chris.banes.dev) for his [PhotoView library](https://github.com/chrisbanes/PhotoView) 

[Casey Horner](https://unsplash.com/@mischievous_penguins) for this photo on [unsplash](https://unsplash.com/photos/1sim8ojvCbE?utm_source=twitter&utm_medium=referral&utm_content=photos-page-share)

[Coil lib](https://coil-kt.github.io/coil/)
 
## License and usage

Feel free to use this file in your code. Just [download](app/src/main/java/com/k2/zoomimageview/ZoomImageView.kt) and add to your project

