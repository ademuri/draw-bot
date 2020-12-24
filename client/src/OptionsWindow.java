import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import javax.swing.BoxLayout;

public class OptionsWindow extends JFrame implements KeyListener {

	private final CanvasViewer canvasViewer;
	private JPanel contentPane;
	private JTextField scaleX;
	private final JLabel lblNewLabel = new JLabel("Scale X");
	private JTextField scaleY;
	
	private final int processingWidth;
	private final int processingHeight;
	private final int leftBound;
	private final int upperBound;
	private JPanel panel_2;
	private JTextField drawSpeed;
	private JLabel lblNewLabel_2;
	private JPanel panel_3;
	private JTextField travelSpeed;
	private JLabel lblNewLabel_3;
	private JPanel panel_4;
	private JTextField lineWidth;
	private JLabel lblNewLabel_4;

	/**
	 * Create the frame.
	 */
	public OptionsWindow(CanvasViewer canvasViewer) {
		this.canvasViewer = canvasViewer;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		
		scaleX = new JTextField();
		scaleX.setFont(new Font("Dialog", Font.PLAIN, 18));
		scaleX.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setScale();
			}
		});
		panel.add(scaleX);
		scaleX.setToolTipText("");
		scaleX.setColumns(10);
		scaleX.addKeyListener(this);
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 18));
		panel.add(lblNewLabel);
		lblNewLabel.setLabelFor(scaleX);
		
		JPanel panel_1 = new JPanel();
		
		scaleY = new JTextField();
		scaleY.setFont(new Font("Dialog", Font.PLAIN, 18));
		scaleY.addKeyListener(this);
		scaleY.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setScale();
			}
		});
		panel_1.add(scaleY);
		scaleY.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Scale Y");
		lblNewLabel_1.setFont(new Font("Dialog", Font.BOLD, 18));
		panel_1.add(lblNewLabel_1);
		lblNewLabel_1.setLabelFor(scaleY);
		
		panel_2 = new JPanel();
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contentPane.add(panel);
		contentPane.add(panel_1);
		contentPane.add(panel_2);
		
		drawSpeed = new JTextField();
		drawSpeed.setFont(new Font("Dialog", Font.PLAIN, 18));
		panel_2.add(drawSpeed);
		drawSpeed.setColumns(10);
		
		lblNewLabel_2 = new JLabel("Draw Speed");
		lblNewLabel_2.setFont(new Font("Dialog", Font.BOLD, 18));
		panel_2.add(lblNewLabel_2);
		
		panel_3 = new JPanel();
		contentPane.add(panel_3);
		
		travelSpeed = new JTextField();
		travelSpeed.setFont(new Font("Dialog", Font.PLAIN, 18));
		travelSpeed.setColumns(10);
		panel_3.add(travelSpeed);
		
		lblNewLabel_3 = new JLabel("Travel Speed");
		lblNewLabel_3.setFont(new Font("Dialog", Font.BOLD, 18));
		panel_3.add(lblNewLabel_3);
		
		panel_4 = new JPanel();
		contentPane.add(panel_4);
		
		lineWidth = new JTextField();
		lineWidth.setText("2");
		lineWidth.setFont(new Font("Dialog", Font.PLAIN, 18));
		lineWidth.setColumns(10);
		lineWidth.addKeyListener(this);
		panel_4.add(lineWidth);
		
		lblNewLabel_4 = new JLabel("Line Width");
		lblNewLabel_4.setFont(new Font("Dialog", Font.BOLD, 18));
		panel_4.add(lblNewLabel_4);
		addKeyListener(this);
		setLocationRight();

		GraphicsConfiguration config = getGraphicsConfiguration();
	    Rectangle bounds = config.getBounds();
	    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
	    System.out.format("width: %d\n", getWidth());
	    processingWidth = bounds.width - insets.right - insets.left - getWidth();
	    processingHeight = bounds.height - insets.top - insets.bottom;
	    leftBound = bounds.x + insets.left;
	    upperBound = bounds.y + insets.top;
	    
	    setLineWidth();
	}
	
	public void init() {
		scaleX.setText(String.format("%.2f", canvasViewer.getScaleX()));
		scaleY.setText(String.format("%.2f", canvasViewer.getScaleY()));
	}
	
	public int getProcessingWidth() {
		return processingWidth;
	}
	
	public int getProcessingHeight() {
		return processingHeight;
	}
	
	public int getLeftBound() {
		return leftBound;
	}
	
	public int getUpperBound() {
		return upperBound;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
	}

	@Override
	public void keyReleased(KeyEvent event) {
	}

	@Override
	public void keyTyped(KeyEvent event) {
		if (event.getSource().equals(scaleX) || event.getSource().equals(scaleY)) {
			setScale();
		} else if (event.getSource().equals(lineWidth)) {
			setLineWidth();
		}
	}
	
	private void setScale() {
		canvasViewer.setScale(scaleX.getText(), scaleY.getText());
	}
	
	private void setLineWidth() {
		canvasViewer.setLineWidth(lineWidth.getText());
	}
	
	private void setLocationRight() {
		GraphicsConfiguration config = getGraphicsConfiguration();
	    Rectangle bounds = config.getBounds();
	    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
	    int x = bounds.x + bounds.width - insets.right - getWidth();
	    int y = bounds.y + insets.top;
	    setLocation(x, y);
	}
}
