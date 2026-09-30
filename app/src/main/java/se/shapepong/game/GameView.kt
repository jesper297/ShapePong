package se.shapepong.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private val bg = Paint().apply { color = Color.rgb(8, 18, 30) }
    private val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val mint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(98, 255, 209) }
    private val coral = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 112, 122) }
    private val muted = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(117, 145, 163) }

    private var playerX = 0f
    private var enemyX = 0f
    private var ballX = 0f
    private var ballY = 0f
    private var vx = 0f
    private var vy = 0f
    private var playerScore = 0
    private var enemyScore = 0
    private var lastFrame = 0L
    private var running = false
    private var shape = 0
    private var baseBallSpeed = 1f
    private var rallyHits = 0
    private val shapes = arrayOf("PLATT", "BÅGE", "VINKEL")

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        playerX = w / 2f
        enemyX = w / 2f
        resetBall(if (Random.nextBoolean()) 1 else -1)
    }

    private fun resetBall(direction: Int) {
        ballX = width / 2f
        ballY = height / 2f
        val speed = min(width, height) * .48f
        baseBallSpeed = speed
        rallyHits = 0
        vx = speed * Random.nextDouble(-.42, .42).toFloat()
        vy = sqrt(speed * speed - vx * vx) * direction
        running = false
        postDelayed({ running = true; lastFrame = System.nanoTime(); invalidate() }, 650)
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)
        drawArena(c)
        drawPaddle(c, enemyX, dp(92f), 0, coral, false)
        drawPaddle(c, playerX, height - dp(122f), shape, mint, true)
        c.drawCircle(ballX, ballY, dp(9f), white)
        drawUi(c)

        if (running) update()
        lastFrame = System.nanoTime()
        postInvalidateOnAnimation()
    }

    private fun drawArena(c: Canvas) {
        muted.strokeWidth = dp(1f)
        muted.alpha = 65
        var x = 0f
        while (x < width) {
            c.drawLine(x, height / 2f, x + dp(12f), height / 2f, muted)
            x += dp(24f)
        }
        muted.alpha = 255
    }

    private fun drawUi(c: Canvas) {
        white.textAlign = Paint.Align.CENTER
        white.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        white.textSize = dp(32f)
        c.drawText("$enemyScore   $playerScore", width / 2f, height / 2f - dp(24f), white)

        muted.textSize = dp(11f)
        muted.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        c.drawText("DATOR      DU", width / 2f, height / 2f + dp(8f), muted)
        muted.textSize = dp(9f)
        val speedMultiplier = (hypot(vx, vy) / baseBallSpeed).coerceAtLeast(1f)
        c.drawText("FART ×${String.format("%.1f", speedMultiplier)}  •  DUELL $rallyHits", width / 2f, height / 2f + dp(25f), muted)

        val cy = height - dp(48f)
        val bw = dp(84f)
        val gap = dp(8f)
        val start = width / 2f - (bw * 1.5f + gap)
        for (i in shapes.indices) {
            val left = start + i * (bw + gap)
            val p = if (i == shape) mint else muted
            p.style = Paint.Style.STROKE
            p.strokeWidth = dp(if (i == shape) 2f else 1f)
            c.drawRoundRect(left, cy - dp(18f), left + bw, cy + dp(18f), dp(10f), dp(10f), p)
            p.style = Paint.Style.FILL
            p.textSize = dp(10f)
            p.textAlign = Paint.Align.CENTER
            p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText(shapes[i], left + bw / 2f, cy + dp(4f), p)
        }
    }

    private fun drawPaddle(c: Canvas, x: Float, y: Float, type: Int, p: Paint, glow: Boolean) {
        val half = dp(54f)
        p.style = Paint.Style.STROKE
        p.strokeWidth = dp(10f)
        p.strokeCap = Paint.Cap.ROUND
        if (glow) p.setShadowLayer(dp(14f), 0f, 0f, p.color)
        setLayerType(LAYER_TYPE_SOFTWARE, p)
        when (type) {
            1 -> {
                val path = Path().apply {
                    moveTo(x - half, y)
                    quadTo(x, y - dp(27f), x + half, y)
                }
                c.drawPath(path, p)
            }
            2 -> {
                val path = Path().apply {
                    moveTo(x - half, y - dp(14f)); lineTo(x, y + dp(12f)); lineTo(x + half, y - dp(14f))
                }
                c.drawPath(path, p)
            }
            else -> c.drawLine(x - half, y, x + half, y, p)
        }
        p.clearShadowLayer()
        p.style = Paint.Style.FILL
    }

    private fun update() {
        if (lastFrame == 0L) return
        val dt = ((System.nanoTime() - lastFrame) / 1_000_000_000f).coerceIn(0f, .025f)
        accelerateOverTime(dt)
        ballX += vx * dt
        ballY += vy * dt
        val r = dp(9f)
        if (ballX < r && vx < 0) { ballX = r; vx = -vx }
        if (ballX > width - r && vx > 0) { ballX = width - r; vx = -vx }

        val aiTarget = ballX + vx * .10f
        val aiSpeed = width * .72f * dt
        enemyX += (aiTarget - enemyX).coerceIn(-aiSpeed, aiSpeed)
        enemyX = enemyX.coerceIn(dp(58f), width - dp(58f))

        if (vy > 0 && ballY >= height - dp(137f) && ballY <= height - dp(105f) && abs(ballX - playerX) < dp(64f)) {
            bounceFromPlayer()
        }
        if (vy < 0 && ballY <= dp(108f) && ballY >= dp(76f) && abs(ballX - enemyX) < dp(64f)) {
            ballY = dp(110f)
            val hit = ((ballX - enemyX) / dp(54f)).coerceIn(-1f, 1f)
            rallyHits++
            val speed = nextBounceSpeed(1.03f)
            val horizontal = (hit * .68f + vx / speed * .16f).coerceIn(-.82f, .82f)
            setBallDirection(speed, horizontal, 1)
        }
        if (ballY < -r) { playerScore++; resetBall(1) }
        if (ballY > height + r) { enemyScore++; resetBall(-1) }
    }

    private fun bounceFromPlayer() {
        val hit = ((ballX - playerX) / dp(54f)).coerceIn(-1f, 1f)
        ballY = height - dp(140f)
        rallyHits++
        val speed = nextBounceSpeed(if (shape == 1) 1.045f else 1.035f)
        when (shape) {
            1 -> { // Båge: den lokala kurvan skickar bollen tydligt utåt.
                val horizontal = (hit * .90f + vx / speed * .08f).coerceIn(-.88f, .88f)
                setBallDirection(speed, horizontal, -1)
            }
            2 -> { // Vinkel: respektive halva ger en fast, skarp diagonal.
                val side = when {
                    hit < -.10f -> -1f
                    hit > .10f -> 1f
                    vx < 0f -> -1f
                    else -> 1f
                }
                setBallDirection(speed, side * .78f, -1)
            }
            else -> { // Platt: klassisk Pong-studs styrd av träffpunkten.
                val horizontal = (hit * .70f + vx / speed * .18f).coerceIn(-.82f, .82f)
                setBallDirection(speed, horizontal, -1)
            }
        }
    }

    private fun accelerateOverTime(dt: Float) {
        val speed = hypot(vx, vy)
        val maxSpeed = min(width, height) * 1.45f
        if (speed <= 0f || speed >= maxSpeed) return
        val target = (speed * (1f + dt * .012f)).coerceAtMost(maxSpeed)
        val scale = target / speed
        vx *= scale
        vy *= scale
    }

    private fun nextBounceSpeed(boost: Float): Float {
        val maxSpeed = min(width, height) * 1.45f
        return (hypot(vx, vy) * boost).coerceAtMost(maxSpeed)
    }

    private fun setBallDirection(speed: Float, horizontalRatio: Float, verticalDirection: Int) {
        val ratio = horizontalRatio.coerceIn(-.90f, .90f)
        vx = speed * ratio
        vy = sqrt((speed * speed - vx * vx).coerceAtLeast(0f)) * verticalDirection
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (e.y > height - dp(78f)) {
                    val bw = dp(84f); val gap = dp(8f)
                    val start = width / 2f - (bw * 1.5f + gap)
                    val selected = ((e.x - start) / (bw + gap)).toInt()
                    if (selected in shapes.indices) shape = selected
                } else playerX = e.x.coerceIn(dp(58f), width - dp(58f))
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> if (e.y < height - dp(78f)) {
                playerX = e.x.coerceIn(dp(58f), width - dp(58f)); invalidate()
            }
        }
        return true
    }
}
