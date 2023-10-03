import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class HomePage implements ActionListener {

    static final String[] operationLabels = {"Matrix Addition", "Matrix Subtraction", "Scalar Multiplication",
            "Multiply Matrices", "Find the Inverse", "Find The Determinant", "Problem Tasks", "Contents"};
    public static final JButton[] operationButtons = {
            new JButton(operationLabels[0]),
            new JButton(operationLabels[1]),
            new JButton(operationLabels[2]),
            new JButton(operationLabels[3]),
            new JButton(operationLabels[4]),
            new JButton(operationLabels[5]),
            new JButton(operationLabels[6]),
            new JButton(operationLabels[7]),
    };

    JDialog dialog = new JDialog();
    HomePage(){
        ChooseOperation();
    }

    private void ChooseOperation () {
        JPanel[] choosePanel = new JPanel[operationButtons.length + 1];
        for (int i = 0; i < choosePanel.length; i++) {
            choosePanel[i] = new JPanel();
        }

        String labelText = "<html><center>Welcome to the Linear Algebra Helper.<br>Please choose the operation.</center></html>";
        JLabel welcomeText = new JLabel(labelText);
        welcomeText.setFont(new Font("Arial", Font.PLAIN, 18));
        choosePanel[0].add(welcomeText);
        choosePanel[0].add(Box.createVerticalStrut(50));

        int buttonWidth = 220;
        int buttonHeight = 35;

        for (int i = 0; i < operationButtons.length; i++) {
            operationButtons[i].setPreferredSize(new Dimension(buttonWidth, buttonHeight));
            operationButtons[i].addActionListener(this);
            choosePanel[i + 1].add(operationButtons[i]);
        }

        dialog.setTitle("Linear Algebra Helper");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        // Add the panel with buttons to the dialog
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(choosePanel.length, 1));
        for (JPanel panel : choosePanel) {
            contentPanel.add(panel);
        }
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < operationButtons.length; i++){
            if (e.getSource()==operationButtons[i]){
                new OperationPage(i);
                dialog.dispose();
            }
        }
    }
}