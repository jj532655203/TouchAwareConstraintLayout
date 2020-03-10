# TouchAwareConstraintLayout



##  1.能力介绍

###  感知缩放移动(场景如:学生将作业图片放大看,移动看其它内容)
###  感知触控笔(场景如:学生落笔了)
###  感知大面积接触/重压事件(场景如:腕部接触或手指用力按住,适合用于判断学生准备动笔了)
###  提供方式给开发者自己定义翻页规则(如本项目Demo定义的4指水平滑动作为翻页)

###  有了以上能力,界面将无需以下按钮
上一页和下一页btn
手指触控与笔写触控的模式切换btn
缩放btn

###  本容器对事件分发机制没有侵入性
不拦截子view的事件
子view.requestDisallowInterceptTouchEvent()等操作后事件流仍然能被本容器感知


##  2.使用姿势
###  A.建议将项目clone下来,研究清楚,方便根据自己的需求进行补充甚至重构



###  B.gradle方式引入

项目根目录的gradle文件
buildscript.repositories{ maven { url "https://jitpack.io" } }
allprojects.repositories{ maven { url "https://jitpack.io" } }


module的gradle文件
implementation 'com.github.jj532655203:TouchAwareConstraintLayout:1.0.1'


CTRL+SHIFT+N找到TouchAwareConstraintLayout.java

请保证容器内部有控件会消费事件,如本项目中的viewpager2的条目view构造函数中添加会消费事件流的view,若条目view想处理事件,再拦截即可
View view = new View(context);
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "SamplePageView onTouch");
                return true;
            }
        });
LayoutParams params = new LayoutParams(-1, -1);
addView(view, params);
		

有问题欢迎issue,我会及时修复并更新版本



##  tips

本项目涉及到的全面的事件分发机制、多点触控等知识是基础有重要的哦，建议clone研究，觉得好请star吧 :）