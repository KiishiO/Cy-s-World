package com.example.signup_test03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleView extends View {
    private List<Particle> particles;
    private Paint paint;
    private Random random;

    public ParticleView(Context context) {
        super(context);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        particles = new ArrayList<>();
        paint = new Paint();
        paint.setColor(getContext().getColor(R.color.cardinal_red));
        paint.setAlpha(50);
        random = new Random();

        // Create initial particles
        for (int i = 0; i < 50; i++) {
            particles.add(createParticle());
        }
    }

    private Particle createParticle() {
        return new Particle(
                random.nextFloat() * getWidth(),
                random.nextFloat() * getHeight(),
                4f + random.nextFloat() * 4,
                1f + random.nextFloat() * 2,
                random.nextFloat() * 360
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint);
            particle.update();
            if (particle.isOutOfBounds(getWidth(), getHeight())) {
                particles.set(i, createParticle());
            }
        }
        invalidate();
    }

    private static class Particle {
        float x;
        float y;
        final float radius;
        final float speed;
        float angle;

        Particle(float x, float y, float radius, float speed, float angle) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.angle = angle;
        }

        void update() {
            x += Math.cos(Math.toRadians(angle)) * speed;
            y += Math.sin(Math.toRadians(angle)) * speed;
            angle += 0.5f;
        }

        boolean isOutOfBounds(float width, float height) {
            return x < -radius || x > width + radius ||
                    y < -radius || y > height + radius;
        }
    }
}

