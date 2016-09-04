package gamelib;

import processing.core.PGraphics;

public class Style {

	private int fill_color;
	private int stroke_color;
	private float strokeWeight;
	
	private boolean noFill;
	private boolean noStroke;
	
	public Style(){
		fill_color		= 0xFFFFFFFF;
		stroke_color	= 0xFF000000;
		strokeWeight	= 1;
		noFill			= false;
		noStroke		= true;
	}
	
	public void fill(int c){
		fill_color = c;
		noFill = false;
	}
	
	public void stroke(int c){
		stroke_color = c;
		noStroke = false;
	}
	
	public void strokeWeight(int w){
		strokeWeight = w;
		noStroke = false;
	}
	
	public void noFill(){
		noFill = true;
	}
	
	public void noStroke(){
		noStroke = true;
	}

	public void apply(){
		apply(GameManager.getMe().getSketch().g);
	}
	
	public void apply(PGraphics g){
		if(noFill){
			g.noFill();
		}
		else{
			g.fill(fill_color);
		}
		if(noStroke){
			g.noStroke();
		}
		else{
			g.stroke(stroke_color);
			g.strokeWeight(strokeWeight);
		}
	}
}
