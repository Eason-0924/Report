import javax.swing.*;


public class ShowPanel {
	public static void main(String[] args) {
        JFrame frame = new JFrame("Show Panel");
        frame.setSize(1200, 100);

        JPanel panel = new user.FunctionBar();
        //panel.setBounds(0, 0, 400, 250);
       
        frame.setContentPane(panel);
        //frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
}
