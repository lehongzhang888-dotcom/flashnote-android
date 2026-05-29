package com.flashnote.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

/**
 * 悬浮球服务 - 在屏幕边缘显示可拖动的闪电图标
 *
 * 使用 WindowManager 实现全局悬浮，点击弹出笔记输入界面。
 * 作为前台服务运行，防止被系统杀死。
 */
class FloatingBallService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingBall: View
    private var layoutParams: WindowManager.LayoutParams? = null

    // 触摸拖动相关
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var isDragging = false

    companion object {
        const val CHANNEL_ID = "floating_ball_channel"
        const val NOTIFICATION_ID = 1001
        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        createFloatingBall()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 显示前台服务通知
        startForeground(NOTIFICATION_ID, createNotification())
        isRunning = true

        // 确保悬浮球已显示
        if (floatingBall.windowToken == null) {
            try {
                windowManager.addView(floatingBall, layoutParams)
            } catch (e: Exception) {
                // 可能已被添加
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            if (floatingBall.windowToken != null) {
                windowManager.removeView(floatingBall)
            }
        } catch (e: Exception) {
            // 视图可能已被移除
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── 悬浮球创建 ──────────────────────────────────────

    /**
     * 创建悬浮球视图并设置触摸事件
     */
    private fun createFloatingBall() {
        // 加载布局
        floatingBall = LayoutInflater.from(this).inflate(R.layout.floating_ball, null)
        val icon = floatingBall.findViewById<ImageView>(R.id.iv_ball_icon)
        icon.setImageResource(R.drawable.ic_lightning)

        // 设置 WindowManager 参数
        val size = dpToPx(48)
        layoutParams = WindowManager.LayoutParams(
            size,
            size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = 0  // 靠右边缘
            y = 200 // 初始 Y 位置
        }

        // 点击和拖动事件
        floatingBall.setOnTouchListener { _, event ->
            onTouchEvent(event)
        }

        // 添加到窗口
        try {
            windowManager.addView(floatingBall, layoutParams)
        } catch (e: Exception) {
            // 权限问题
        }
    }

    /**
     * 处理触摸事件 - 支持拖动和点击
     */
    private fun onTouchEvent(event: MotionEvent): Boolean {
        layoutParams ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                initialX = layoutParams!!.x.toFloat()
                initialY = layoutParams!!.y.toFloat()
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble())

                if (distance > 10) {
                    isDragging = true
                    layoutParams!!.x = (initialX + dx).toInt()
                    layoutParams!!.y = (initialY + dy).toInt()

                    // 限制在屏幕范围内
                    val displayMetrics = resources.displayMetrics
                    layoutParams!!.x = layoutParams!!.x.coerceAtLeast(0)
                    layoutParams!!.y = layoutParams!!.y.coerceAtLeast(0)
                    layoutParams!!.y = layoutParams!!.y.coerceAtMost(
                        displayMetrics.heightPixels - dpToPx(48)
                    )

                    windowManager.updateViewLayout(floatingBall, layoutParams)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    // 点击事件：打开笔记输入界面
                    openNoteInput()
                } else {
                    // 拖动结束后，吸附到右边缘
                    snapToRight()
                }
                return true
            }
        }
        return false
    }

    /**
     * 吸附到屏幕右边缘
     */
    private fun snapToRight() {
        val displayMetrics = resources.displayMetrics
        val ballSize = dpToPx(48)
        layoutParams?.x = displayMetrics.widthPixels - ballSize
        layoutParams?.let {
            try {
                windowManager.updateViewLayout(floatingBall, it)
            } catch (e: Exception) {
                // 忽略
            }
        }
    }

    /**
     * 打开笔记输入界面
     */
    private fun openNoteInput() {
        val intent = Intent(this, NoteInputActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    // ─── 通知 ────────────────────────────────────────────

    /**
     * 创建通知渠道（Android 8+ 必须）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "闪电记悬浮球",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮球后台服务通知"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("闪电记")
            .setContentText("悬浮球已开启，点击记录灵感")
            .setSmallIcon(R.drawable.ic_lightning)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .build()
    }

    // ─── 工具 ────────────────────────────────────────────

    /**
     * dp 转 px
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
