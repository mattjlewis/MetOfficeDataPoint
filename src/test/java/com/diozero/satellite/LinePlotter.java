package com.diozero.satellite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.hipparchus.util.FastMath;

public class LinePlotter {
	private int width;
	private int height;
	private int radius;
	private BufferedImage image;
	private Graphics2D graphics;
	
	public static void main(String[] args) {
		LinePlotter pt = new LinePlotter(400);
		
		int approach_deg = 273;
		int departure_deg = 32;
		pt.drawLine(approach_deg, departure_deg, 3);
		pt.writeImage("testImage.jpg");
	}
	
	public LinePlotter(int width) {
		this.width = width;
		height = this.width;
		radius = width/2;

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0,  0, width, height);
		
		graphics.setColor(Color.WHITE);
		graphics.drawArc(0, 0, width, height, 0, 360);
		// Draw a line in the middle from left to right
		drawLine(-radius, 0, radius, 0);
		// Draw a line in the middle from bottom to top
		drawLine(0, -radius, 0, radius);

		// Draw an X in the middle
		drawX(0, 0, 10);
		// Draw an X on the left middle
		drawX(-radius+5, 0, 10);
		// Draw an X on the right middle
		drawX(radius-5, 0, 10);
		// Draw an X on the bottom middle
		drawX(0, -radius+5, 10);
		// Draw an X on the top middle
		drawX(0, radius-5, 10);
		
		graphics.setColor(Color.RED);
	}
	
	public void drawLine(int approachDeg, int departureDeg, int width) {
		double approach_rad = FastMath.toRadians(approachDeg);
		double departure_rad = FastMath.toRadians(departureDeg);
		int x1 = (int) (radius * Math.sin(approach_rad));
		int y1 = (int) (radius * Math.cos(approach_rad));
		int x2 = (int) (radius * Math.sin(departure_rad));
		int y2 = (int) (radius * Math.cos(departure_rad));
		drawLine(x1, y1, x2, y2, width);
	}

	public void drawX(int x, int y, int length) {
		drawLine(x - length, y + length, x + length, y - length);
		drawLine(x - length, y - length, x + length, y + length);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		graphics.drawLine(translateX(x1), translateY(y1), translateX(x2), translateY(y2));
	}

	public void drawLine(int x1, int y1, int x2, int y2, int width) {
		Stroke s = graphics.getStroke();
		graphics.setStroke(new BasicStroke(width));
		drawLine(x1, y1, x2, y2);
		graphics.setStroke(s);
	}
	
	public void writeImage(String filename) {
		try {
			ImageIO.write(image, "JPG", new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int translateX(int x) {
		return x + radius;
	}

	private int translateY(int y) {
		return height - (y + radius);
	}
}
