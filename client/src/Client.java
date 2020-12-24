import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGGraphicsElement;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.anim.dom.SVGOMPolylineElement;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

import controlP5.CallbackEvent;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlListener;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Slider;
import controlP5.Textfield;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;


public class Client extends PApplet  {
	// Develop in Eclipse following https://happycoding.io/tutorials/java/processing-in-java
	// Note: Batik uses a really old version of the W3C Document APIs: https://stackoverflow.com/questions/13676937/how-to-find-package-org-w3c-dom-svg
	
	private static final int windowX = 3600;
	private static final int windowY = 1800;
	private static final int SLIDER_HEIGHT = 100;
	private static final int SLIDER_MARGIN = 20;
	private static final int PANEL_WIDTH = 200;
	
	private static final int CANVAS_MARGIN = 50;
	
	private static final int FONT_SIZE = 40;
	
	// Note: all physical distances are in millimeters
	private static final float CANVAS_X = 457; // 18"
	private static final float CANVAS_Y = 305; // 12"
	private PVector canvasStart = new PVector(CANVAS_MARGIN, CANVAS_MARGIN);
	private float canvasScale = 1;
	
	private final List<SVGGraphicsElement> svgGraphics = new ArrayList<>();
	
	private int prevSliderValue = 0;
	
	private ControlP5 controlP5;
	private SmartControl<Slider> slider;
	private SmartControl<Textfield> scaleXField;
	private SmartControl<Textfield> scaleYField;
	
	public void settings() {
		size(windowX, windowY);
	}
	
	public void setup() {
		double usableX = windowX - (PANEL_WIDTH + CANVAS_MARGIN * 2);
		double usableY = windowY - (SLIDER_HEIGHT + SLIDER_MARGIN + CANVAS_MARGIN * 2);
		
		// Make canvas as big as possible using whole-number scaling, or fractional scaling if canvas is bigger than the window
		if (CANVAS_X > usableX || CANVAS_Y > usableY) {
			canvasScale = (float) (1 / Math.max(Math.ceil(CANVAS_X / usableX) , Math.ceil(CANVAS_Y / usableY)));
		} else {
			canvasScale = (float) Math.min(Math.floor(usableX / CANVAS_X), Math.floor(usableY / CANVAS_Y));
		}
		System.out.format("Using canvas scale factor of %.2f\n", canvasScale);
				
		controlP5 = new ControlP5(this);
		slider = new SmartControl<>(controlP5.addSlider("lineNumber")
			.setPosition(50, windowY - SLIDER_HEIGHT - SLIDER_MARGIN)
			.setHeight(SLIDER_HEIGHT)
			.setWidth((windowX * 8) / 10)
			.setRange(0,  1)
			.setSliderMode(Slider.FLEXIBLE)
			.snapToTickMarks(true), this::sliderChanged);
		
		scaleXField = new SmartControl<>(controlP5.addTextfield("scaleX")
				.setPosition(CANVAS_X * canvasScale + CANVAS_MARGIN * 3, 50)
				.setSize(FONT_SIZE * 4, FONT_SIZE * 2)
				.setFont(new ControlFont(createFont("FreeMono", FONT_SIZE), FONT_SIZE))
				);
		scaleXField.getControl().getCaptionLabel().set("Scale X").setSize(FONT_SIZE).setPaddingX(10);
		
		scaleYField = new SmartControl<>(controlP5.addTextfield("scaleY")
				.setPosition(CANVAS_X * canvasScale + CANVAS_MARGIN * 3, 50 + scaleXField.getControl().getHeight() * (float) 2.0)
				.setSize(FONT_SIZE * 4, FONT_SIZE * 2)
				.setFont(new ControlFont(createFont("FreeMono", FONT_SIZE), FONT_SIZE))
				);
		scaleYField.getControl().getCaptionLabel().set("Scale Y").setSize(FONT_SIZE).setPaddingX(10);
		
		slider.getControl().getValueLabel().setSize(FONT_SIZE);
		slider.getControl().getCaptionLabel().set("Line #").setSize(FONT_SIZE).setColor(255).setPaddingX(10);
		
		final Document doc;
		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		     //doc = f.createDocument("example.svg");
		     doc = f.createDocument("torus.svg");
		} catch (IOException ex) {
			ex.printStackTrace();
			exit();
			return;
		}
		
		// See https://stackoverflow.com/questions/26027313/how-to-load-and-parse-svg-documents
		UserAgent userAgent = new UserAgentAdapter();
	    DocumentLoader loader = new DocumentLoader(userAgent);
	    BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
	    bridgeContext.setDynamicState(BridgeContext.DYNAMIC);

	    // Enable CSS- and SVG-specific enhancements.
	    (new GVTBuilder()).build(bridgeContext, doc);
		
		Element rootElement = doc.getDocumentElement();
		traverse(rootElement);
		
		svgGraphics.sort(Comparator.<SVGGraphicsElement>comparingDouble(Client::svgLength).reversed());
		
		slider.getControl().setNumberOfTickMarks(svgGraphics.size())
			.setRange(0, svgGraphics.size());
		
		float scale = 1;
		float svgWidth = ((SVGOMSVGElement) rootElement).getWidth().getBaseVal().getValue();
		float svgHeight = ((SVGOMSVGElement) rootElement).getWidth().getBaseVal().getValue();
		scale = Math.min(svgWidth / CANVAS_X, svgHeight / CANVAS_Y);
		scaleXField.getControl()
		.setText(String.format("%.2f", scale))
		.update();
		scaleYField.getControl()
		.setText(String.format("%.2f", scale))
		.update();
	}
	
	private static float svgLength(Element element) {
		if (element instanceof SVGOMPathElement) {
			SVGOMPathElement path = (SVGOMPathElement) element;
			return path.getTotalLength();
		} else if (element instanceof SVGOMPolylineElement) {
			SVGOMPolylineElement polyline = (SVGOMPolylineElement) element;
			float length = 0;
			for (int i = 0; i < polyline.getPoints().getNumberOfItems() - 1; i++) {
				SVGPoint start = polyline.getPoints().getItem(i);
				SVGPoint end = polyline.getPoints().getItem(i + 1);
				length +=  Math.sqrt(Math.pow(end.getX() - start.getX(), 2) + Math.pow(end.getY() - start.getY(), 2));
			}
			return length;
		}
		
		return 0;
	}
	
	private void traverse(Element element) {
		if (element instanceof SVGGraphicsElement) {
			svgGraphics.add((SVGGraphicsElement) element);
		}
		
		for (int i = 0; i < element.getChildNodes().getLength(); i++) {
			Node child = element.getChildNodes().item(i);
			if (child instanceof Element) {
				traverse((Element) child);
			}
		}
	}
	
	private static float parseFloatOrDefault(String text, float defaultValue) {
		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	private void canvasLine(float x1, float y1, float x2, float y2) {
		float scaleX = parseFloatOrDefault(scaleXField.getControl().getText(), 1);
		float scaleY = parseFloatOrDefault(scaleYField.getControl().getText(), 1);
		line(canvasStart.x + x1 * canvasScale * scaleX, canvasStart.y + y1 * canvasScale * scaleY, canvasStart.x +  x2 * canvasScale * scaleX, canvasStart.y + y2 * canvasScale * scaleY);
	}
	
	private void drawSvgGraphic(SVGGraphicsElement element) {
		if (element instanceof SVGOMPathElement) {
			SVGOMPathElement pathElement = (SVGOMPathElement) element;
			float length = pathElement.getTotalLength();
			float step = length / 10;
			for (float i = 0; i < length; i += step) {
				float endLength = i + step;
				if (endLength > length) {
					endLength = length;
				}
				
				SVGPoint start = pathElement.getPointAtLength(i);
				SVGPoint end = pathElement.getPointAtLength(endLength);
				canvasLine(start.getX(), start.getY(), end.getX(), end.getY());
			}
		} else if (element instanceof SVGOMPolylineElement) {
			SVGOMPolylineElement polyline = (SVGOMPolylineElement) element;
			SVGPointList pointList = polyline.getPoints();
			for (int i = 0; i < pointList.getNumberOfItems() - 1; i++) {
				SVGPoint start = pointList.getItem(i);
				SVGPoint end = pointList.getItem(i + 1);
				canvasLine(start.getX(), start.getY(), end.getX(), end.getY());
			}
		}
	}
	
	public void draw() {
		clear();
		background(50, 50, 50);
		fill(255);
		rect(canvasStart.x, canvasStart.y, canvasStart.x + CANVAS_X * canvasScale, canvasStart.y + CANVAS_Y * canvasScale);
		for (int i = 0; i < slider.getValue(); i++) {
			drawSvgGraphic(svgGraphics.get(i));
		}
	}
	
	public void keyPressed() {
		if (key == ESC) {
			exit();
		}
	}
	
	public void sliderChanged(ControlEvent arg0) {
		int sliderValue = (int) arg0.getValue();
		if (prevSliderValue != sliderValue) {
			redraw();
		}
		
		prevSliderValue = sliderValue;
	}

	public static void main(String[] args) {
		String[] processingArgs = {"Iconograph CNC Client"};
		Client client = new Client();
		PApplet.runSketch(processingArgs, client);
	}
}
