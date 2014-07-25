package com.gulu.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

public class IconGenerator {
	
	@Deprecated
	public static Bitmap generateCircleIcon(int radius) {
		float blureRadius = 2.0f;
		float strokeWidth = 2.0f;
		int padding = 8;
		
		int width = (int) (radius * 2 + blureRadius / 2 + strokeWidth / 2)
				+ padding;
		int height = width;
		
		Bitmap icon = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(icon);
		
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setStyle(Style.FILL);
		p.setColor(Color.TRANSPARENT);
		Rect r = new Rect(0, 0, width, height);
		canvas.drawRect(r, p);
		

		p.setStyle(Style.FILL);
		p.setColor(Color.parseColor("#7ACCED"));
		canvas.drawCircle(width / 2, height / 2, radius, p);
		
		p.setStyle(Style.STROKE);
		p.setStrokeWidth(strokeWidth);
		p.setColor(Color.parseColor("#4C99CB"));
		p.setMaskFilter(new BlurMaskFilter(blureRadius, Blur.SOLID));
		canvas.drawCircle(width / 2, height / 2, radius, p);
		
		float slice = strokeWidth / 2 + 1.5f;
		double angle = 120;
		p.setStyle(Style.FILL);
		p.setColor(Color.parseColor("#35B3E5"));
		
		float xOrigin = (float) (width / 2 + Math.cos(angle) * slice);
		float yOrigin = (float) (height / 2 + Math.sin(angle) * slice);
		canvas.drawCircle(xOrigin, yOrigin, radius - slice, p);
		
		return icon;
	}
	
	public static Bitmap generateBlueCircleIcon(int radius) {
		float blureRadius = 2.0f;
		float strokeWidth = 2.0f;
		float slice = strokeWidth / 2 + 2.0f;
		double angle = 25;
		int padding = 8;
		
		int width = (int) (radius * 2 + blureRadius / 2 + strokeWidth / 2)
				+ padding;
		int height = width;
		
		float xOrigin = (float) width / 2;
		float yOrigin = (float) height / 2;
		
		Bitmap icon = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(icon);
		
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setStyle(Style.FILL);
		p.setColor(Color.TRANSPARENT);
		Rect r = new Rect(0, 0, width, height);
		canvas.drawRect(r, p);

		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
		p.setShader(null);
		p.setStrokeWidth(strokeWidth);
		p.setColor(Color.parseColor("#4C99CB"));
		// p.setMaskFilter(new BlurMaskFilter(blureRadius, Blur.SOLID));
		canvas.drawCircle(width / 2, height / 2, radius, p);
		
		float x0 = (float) (width / 2 - Math.cos(angle) * radius);
		float y0 = (float) (height / 2 - Math.sin(angle) * radius);
		float x1 = (float) (width / 2 + Math.cos(angle) * radius);
		float y1 = (float) (height / 2 + Math.sin(angle) * radius);
		p.setStyle(Style.FILL);
		p.setShader(new LinearGradient(x0, y0, x1, y1, Color
				.parseColor("#7ACCED"), Color.parseColor("#35B3E5"),
				TileMode.CLAMP));
		p.setColor(Color.parseColor("#7ACCED"));
		canvas.drawCircle(width / 2, height / 2, radius - strokeWidth / 2, p);
		

		p.setAntiAlias(true);
		p.setStyle(Style.FILL);
		p.setShader(null);
		p.setColor(Color.parseColor("#35B3E5"));
		
		canvas.drawCircle(xOrigin, yOrigin, radius - slice, p);

		return icon;
	}
}
