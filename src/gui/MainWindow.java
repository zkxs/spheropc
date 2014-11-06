package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainWindow extends JFrame
{
	/** Class version number */
	//private static final long serialVersionUID = 0_0L; //TODO: Begin keeping track of this before release
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				try
				{
					JFrame window = new MainWindow();
					window.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	MainWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, -1, -1); // move/resize frame (resizing is done later in pack())
		
		JPanel contentPane = new JPanel();
		
		contentPane.setBorder(new LineBorder(Color.BLACK, 5));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		contentPane.setMinimumSize(new Dimension(200, 100));
		contentPane.setPreferredSize(contentPane.getMinimumSize());
		
		setContentPane(contentPane);
		pack();
		
		/* set minimum size based off of contents instead of worrying about
		 * an arbitrarily dimensioned border
		 */
		setMinimumSize(getSize());
		contentPane.setPreferredSize(new Dimension(800, 600));
		pack();
		
		JLabel label = new JLabel("Hello, World!");
		label.setFont(label.getFont().deriveFont(64.0f));
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setVerticalAlignment(JLabel.CENTER);
		contentPane.add(Box.createHorizontalGlue());
		contentPane.add(label);
		contentPane.add(Box.createHorizontalGlue());
		//contentPane.add(new JLabel("faq"));
	}
	
	
}
