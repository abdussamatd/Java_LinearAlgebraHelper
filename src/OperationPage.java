import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.border.Border;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class OperationPage {
    String[] operationLabels;
    JButton[] operationButtons;
    JPanel rightSide;
    CardLayout cardLayout;

    static double[][][][] matrices;

    OperationPage(int operationIndex) {
        operationLabels = HomePage.operationLabels;
        operationButtons = HomePage.operationButtons;
        build(operationIndex);
    }

    private void build(int index) {
        JFrame frame = new JFrame("Linear Algebra Helper");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Left Sidebar Panel
        JPanel leftSidebar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 1.0; // Buttons will take equal horizontal space
        gbc.weighty = 1.0; // Buttons will take equal vertical space

        cardLayout = new CardLayout();
        rightSide = new JPanel(cardLayout);
        rightSide.setBackground(Color.WHITE);

        // Create panels for each operation and add them to the rightSide panel
        for (int i = 0; i < operationLabels.length; i++) {
            matrices = new double[i][3][][];
            JPanel operationPanel = createOperationPanel(i);
            JScrollPane scrollPane = new JScrollPane(operationPanel); // Wrap the panel with a JScrollPane
            rightSide.add(scrollPane, String.valueOf(i));

            JButton button = new JButton(operationLabels[i]);
            button.setPreferredSize(new Dimension(200, 30));
            leftSidebar.add(button, gbc);

            final int operationIndex = i;
            button.addActionListener(e -> {
                // Show the corresponding panel based on the operationIndex
                cardLayout.show(rightSide, String.valueOf(operationIndex));

                // Highlight the selected button
                highlightSelectedButton(operationIndex);
            });

            operationButtons[i] = button; // Update the operationButtons array
        }
        operationButtons[index].doClick();
        frame.add(leftSidebar, BorderLayout.WEST);
        frame.add(rightSide, BorderLayout.CENTER);

        frame.setSize(1000, 600); // Set the size of the frame according to your requirements
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
    }


    private void highlightSelectedButton(int selectedIndex) {
        // Reset the border of all buttons
        for (JButton operationButton : operationButtons) {
            operationButton.setBorder(null);
        }
        // Highlight the selected button using a border
        Border selectedBorder = BorderFactory.createLineBorder(Color.BLUE, 2);
        operationButtons[selectedIndex].setBorder(selectedBorder);
    }

    private JPanel createOperationPanel(int operationIndex) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        switch (operationIndex) {
            case 0, 1 -> panel.add(createAdditionSubtractionPanels(operationIndex));
            case 2 -> panel.add(createScalarMultiplicationPanel(operationIndex));
            case 3 -> panel.add(createMultiplyMatricesPanel(operationIndex));
            case 4 -> panel.add(createInversePanel(operationIndex));
            case 5 -> panel.add(createDeterminantPanel(operationIndex));
            case 6 -> panel.add(createPDFPanel());
            case 7 -> panel.add(createContentPanel());
        }
        return panel;
    }

    private ScrollPane createAdditionSubtractionPanels(int index) {
        ScrollPane scrollPane = new ScrollPane();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel matrixPanel = new JPanel();

        JButton matrixAButton = new JButton("Matrix A");
        JButton newMatrixAButton = new JButton("New");

        matrixPanel.add(matrixAButton);
        matrixPanel.add(newMatrixAButton);
        matrixPanel.add(Box.createHorizontalStrut(30));

        JButton matrixBButton = new JButton("Matrix B");
        JButton newMatrixBButton = new JButton("New");
        matrixPanel.add(matrixBButton);
        matrixPanel.add(newMatrixBButton);
        matrixPanel.add(Box.createHorizontalStrut(50));

        JButton calculateButton = new JButton("Calculate");

        matrixPanel.add(calculateButton);
        panel.add(matrixPanel);

        JPanel resultPanel = new JPanel();

        matrixAButton.addActionListener(e -> {
            if (matrices[index][0] != null) OperationPage.showMatrix(matrices[index][0]); else newMatrixAButton.doClick();
        });
        matrixBButton.addActionListener(e -> {
            if (matrices[index][1] != null) OperationPage.showMatrix(matrices[index][1]); else newMatrixBButton.doClick();
        });
        newMatrixAButton.addActionListener(e -> matrices[index][0] = OperationPage.newMatrix());
        newMatrixBButton.addActionListener(e -> {
            if (matrices[index][0] != null) matrices[index][1] = OperationPage.setElements(matrices[index][0]);
        });

        // Add action listener for the "Calculate" button
        calculateButton.addActionListener(e -> {
            if (matrices[index][0] == null || matrices[index][1] == null) {
                JOptionPane.showMessageDialog(panel, "Please create both Matrix A and Matrix B first.");
            } else {
                matrices[index][2] = matrixAdditionSubstruction(matrices[index][0], matrices[index][1], index);
                if (matrices[index][2] != null) {
                    resultPanel.removeAll();
                    showMatrix(resultPanel, matrices[index][2]);
                    resultPanel.revalidate();
                    resultPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(panel, "Matrix dimensions are not compatible.");
                }
            }
        });
        panel.add(resultPanel);
        scrollPane.add(panel);
        return scrollPane;
    }

    private ScrollPane createScalarMultiplicationPanel(int index){
        ScrollPane scrollPane = new ScrollPane();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel matrixPanel = new JPanel();

        JButton matrixButton = new JButton("Matrix");
        JButton newMatrixButton = new JButton("New");

        matrixPanel.add(matrixButton);
        matrixPanel.add(newMatrixButton);
        matrixPanel.add(Box.createHorizontalStrut(30));

        matrixPanel.add(new JLabel("Scalar number"));
        JTextField scalarNumber = new JTextField(10);
        matrixPanel.add(scalarNumber);
        matrixPanel.add(Box.createHorizontalStrut(50));

        JButton calculateButton = new JButton("Calculate");

        matrixPanel.add(calculateButton);
        panel.add(matrixPanel);

        JPanel resultPanel = new JPanel();

        matrixButton.addActionListener(e -> {
            if (matrices[index][0] != null) OperationPage.showMatrix(matrices[index][0]); else newMatrixButton.doClick();
        });
        newMatrixButton.addActionListener(e -> matrices[index][0] = OperationPage.newMatrix());
        // Add action listener for the "Calculate" button
        calculateButton.addActionListener(e -> {
            if (matrices[index][0] == null || scalarNumber.getText().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter the Matrix and scalar number first.");
            } else {
                try{
                    double scalar = Double.parseDouble(scalarNumber.getText());
                    matrices[index][2] = multiplyByScalar(matrices[index][0],scalar);
                    if (matrices[index][2] != null) {
                        resultPanel.removeAll();
                        showMatrix(resultPanel, matrices[index][2]);
                        resultPanel.revalidate();
                        resultPanel.repaint();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Matrix dimensions are not compatible.");
                    }
                }catch (Exception exception){
                    JOptionPane.showMessageDialog(panel, "Wrong Scalar number!");
                    throw exception;
                }
            }
        });
        panel.add(resultPanel);
        scrollPane.add(panel);
        return scrollPane;
    }
    private ScrollPane createMultiplyMatricesPanel(int index){
        ScrollPane scrollPane = new ScrollPane();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel matrixPanel = new JPanel();

        JButton matrixAButton = new JButton("Matrix A");
        JButton newMatrixAButton = new JButton("New");

        matrixPanel.add(matrixAButton);
        matrixPanel.add(newMatrixAButton);
        matrixPanel.add(Box.createHorizontalStrut(30));

        JButton matrixBButton = new JButton("Matrix B");
        JButton newMatrixBButton = new JButton("New");
        matrixPanel.add(matrixBButton);
        matrixPanel.add(newMatrixBButton);
        matrixPanel.add(Box.createHorizontalStrut(50));

        JButton calculateButton = new JButton("Calculate");

        matrixPanel.add(calculateButton);
        panel.add(matrixPanel);

        JPanel resultPanel = new JPanel();

        matrixAButton.addActionListener(e -> {
            if (matrices[index][0] != null) OperationPage.showMatrix(matrices[index][0]); else newMatrixAButton.doClick();
        });
        matrixBButton.addActionListener(e -> {
            if (matrices[index][1] != null) OperationPage.showMatrix(matrices[index][1]); else newMatrixBButton.doClick();
        });
        newMatrixAButton.addActionListener(e -> matrices[index][0] = OperationPage.newMatrix());
        newMatrixBButton.addActionListener(e -> {
            if (matrices[index][0] != null) {
                JTextField wField = new JTextField(5); //col field
                int newRow = matrices[index][0][0].length;
                JPanel[] choosePanel = new JPanel [2];
                choosePanel [0] = new JPanel();
                choosePanel [1] = new JPanel();

                choosePanel[0].add(new JLabel("Enter Dimensitions") );

                choosePanel[1].add(new JLabel("Rows:"));
                choosePanel[1].add(new JLabel(String.valueOf(newRow)));
                choosePanel[1].add(Box.createHorizontalStrut(15)); // a spacer
                choosePanel[1].add(new JLabel("Cols:"));
                choosePanel[1].add(wField);
                int result = JOptionPane.showConfirmDialog(null, choosePanel,
                        null, JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if(result == 0) {
                    try{
                        int newCol = Integer.parseInt(wField.getText());
                        matrices[index][1] = OperationPage.setElements(new double[newRow][newCol]);
                    }catch(Exception exception){
                        JOptionPane.showMessageDialog(panel, "Please, enter the correct number!");
                    }
                }
            }
        });

        calculateButton.addActionListener(e -> {
            if (matrices[index][0] == null || matrices[index][1] == null) {
                JOptionPane.showMessageDialog(panel, "Please create both Matrix A and Matrix B first.");
            } else {
                int sum, row = matrices[index][0].length, col = matrices[index][0][0].length,
                        col2 = matrices[index][1][0].length;
                matrices[index][2] = new double [row][col2];
                for ( int i = 0 ; i < row ; i++ )
                {
                    for ( int j = 0 ; j < col2 ; j++ )
                    {
                        sum = 0;
                        for ( int k = 0 ; k < col ; k++ )
                        {
                            sum +=  matrices[index][0][i][k]*matrices[index][1][k][j];
                        }
                        matrices[index][2][i][j] = sum;
                    }
                }
                if (matrices[index][2] != null) {
                    resultPanel.removeAll();
                    showMatrix(resultPanel, matrices[index][2]);
                    resultPanel.revalidate();
                    resultPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(panel, "Matrix dimensions are not compatible.");
                }
            }
        });
        panel.add(resultPanel);
        scrollPane.add(panel);
        return scrollPane;
    }


    private ScrollPane createInversePanel(int index){
        ScrollPane scrollPane = new ScrollPane();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel matrixPanel = new JPanel();

        JButton matrixButton = new JButton("Matrix");
        JButton newMatrixButton = new JButton("New");

        matrixPanel.add(matrixButton);
        matrixPanel.add(newMatrixButton);

        matrixPanel.add(Box.createHorizontalStrut(50));

        JButton calculateButton = new JButton("Calculate");

        matrixPanel.add(calculateButton);
        panel.add(matrixPanel);

        JPanel resultPanel = new JPanel();

        matrixButton.addActionListener(e -> {
            if (matrices[index][0] != null) OperationPage.showMatrix(matrices[index][0]); else newMatrixButton.doClick();
        });
        newMatrixButton.addActionListener(e -> matrices[index][0] = OperationPage.newMatrix());
        // Add action listener for the "Calculate" button
        calculateButton.addActionListener(e -> {
            if (matrices[index][0] == null || matrices[index][0].length != matrices[index][0][0].length) {
                JOptionPane.showMessageDialog(panel, "Please enter the square Matrix.");
            } else {
                if(getDeterminant(matrices[index][0]) == 0) {
                    JOptionPane.showMessageDialog(null, """
                Your Matrix hasn't an inverse one

                Because its value = 0""");
                }else {
                    matrices[index][2] = inverse(matrices[index][0]);
                    if (matrices[index][2] != null) {
                        resultPanel.removeAll();
                        showMatrix(resultPanel, matrices[index][2]);
                        resultPanel.revalidate();
                        resultPanel.repaint();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Matrix dimensions are not compatible.");
                    }
                }
            }
        });
        panel.add(resultPanel);
        scrollPane.add(panel);
        return scrollPane;
    }

    private ScrollPane createDeterminantPanel(int index){
        ScrollPane scrollPane = new ScrollPane();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel matrixPanel = new JPanel();

        JButton matrixButton = new JButton("Matrix");
        JButton newMatrixButton = new JButton("New");

        matrixPanel.add(matrixButton);
        matrixPanel.add(newMatrixButton);

        matrixPanel.add(Box.createHorizontalStrut(50));

        JButton calculateButton = new JButton("Calculate");

        matrixPanel.add(calculateButton);
        panel.add(matrixPanel);

        JPanel resultPanel = new JPanel();

        matrixButton.addActionListener(e -> {
            if (matrices[index][0] != null) OperationPage.showMatrix(matrices[index][0]); else newMatrixButton.doClick();
        });
        newMatrixButton.addActionListener(e -> matrices[index][0] = OperationPage.newMatrix());
        // Add action listener for the "Calculate" button
        calculateButton.addActionListener(e -> {
            if (matrices[index][0] == null || matrices[index][0].length != matrices[index][0][0].length) {
                JOptionPane.showMessageDialog(panel, "Please enter the square Matrix.");
            } else {
                double determinant = getDeterminant(matrices[index][0]);
                String value = String.format("%.2f", determinant);
                resultPanel.removeAll();
                JLabel label = new JLabel("Determinant is equal to: "+value);
                label.setFont(new Font("Arial", Font.PLAIN, 18));
                resultPanel.add(label);
                resultPanel.revalidate();
                resultPanel.repaint();
            }
        });
        panel.add(resultPanel);
        scrollPane.add(panel);
        return scrollPane;
    }

    private JPanel createPDFPanel() {
        String filePath = "files/pdf/", fileExt = ".pdf";

        JPanel panel = new JPanel(new BorderLayout());

        // Create a JComboBox to select the file
        String[] fileNames = {"Matrix Addition", "Matrix Subtraction", "Scalar Multiplication",
                "Multiply Matrices", "Find the Inverse", "Find The Determinant"}; // Replace with actual file names
        JComboBox<String> fileComboBox = new JComboBox<>(fileNames);

        // Create a JTextArea to display the file content
        JTextArea textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        // Create a JScrollPane to wrap the JTextArea
        JScrollPane textScrollPane = new JScrollPane(textArea);

        // Create a JPanel to hold the PDF display
        JPanel pdfDisplayPanel = new JPanel(new BorderLayout());

        // Initially, display the textArea
        panel.add(textScrollPane, BorderLayout.CENTER);

        // Add the JComboBox and the JScrollPane to the panel
        panel.add(fileComboBox, BorderLayout.NORTH);
        panel.add(textScrollPane, BorderLayout.CENTER);
        panel.add(pdfDisplayPanel, BorderLayout.CENTER);

        // Add an ActionListener to the JComboBox to update the displayed content when a different file is selected
        fileComboBox.addActionListener(e -> {
            String fullFileName = filePath + fileComboBox.getSelectedIndex() + fileExt;
            displayPDFContent(fullFileName, textArea, pdfDisplayPanel);
            // Toggle the visibility of the components based on the file type
            boolean isPDF = fullFileName.endsWith(".pdf");
            textScrollPane.setVisible(!isPDF);
            pdfDisplayPanel.setVisible(isPDF);
            panel.revalidate();
            panel.repaint();
        });

        return panel;
    }

    private void displayPDFContent(String fileName, JTextArea textArea, JPanel pdfDisplayPanel) {
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            assert inputStream != null;
            PDDocument document = PDDocument.load(inputStream);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int numPages = document.getNumberOfPages();

            // Create a JPanel to hold the scaled PDF pages as JLabels
            JPanel pdfPagesPanel = new JPanel(new GridLayout(numPages, 1));

            // Get the width of the panel and calculate the scaling factor
            int panelWidth = pdfDisplayPanel.getWidth();
            double scale = 1.0;

            for (int i = 0; i < numPages; i++) {
                document.getPage(i);
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 150);

                // Calculate the scaling factor based on the panel width and page width
                int pageWidth = image.getWidth();
                if (pageWidth > panelWidth) {
                    scale = (double) panelWidth / pageWidth;
                }

                // Scale down the image
                int scaledWidth = (int) (pageWidth * scale);
                int scaledHeight = (int) (image.getHeight() * scale);
                BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = scaledImage.createGraphics();
                graphics.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
                graphics.dispose();

                JLabel pageLabel = new JLabel(new ImageIcon(scaledImage));
                pdfPagesPanel.add(pageLabel);
            }

            // Create a JScrollPane to wrap the JPanel
            JScrollPane pdfScrollPane = new JScrollPane(pdfPagesPanel);

            // Add the JScrollPane with scaled PDF pages to the PDF display panel
            pdfDisplayPanel.removeAll();
            pdfDisplayPanel.add(pdfScrollPane, BorderLayout.CENTER);
            pdfDisplayPanel.revalidate();
            pdfDisplayPanel.repaint();

            document.close();
        } catch (IOException e) {
            textArea.setText("Error reading the PDF file: " + fileName);
        }
    }

    private JPanel createContentPanel() {
        String filePath = "files/txt/", fileExt = ".txt";
        JPanel panel = new JPanel(new BorderLayout());

        // Create a JComboBox to select the file
        String[] fileNames = {"Matrix Addition", "Matrix Subtraction", "Scalar Multiplication",
                "Multiply Matrices", "Find the Inverse", "Find The Determinant"}; // Replace with actual file names
        JComboBox<String> fileComboBox = new JComboBox<>(fileNames);

        // Create a JTextArea to display the file content
        JTextArea textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        // Create a JScrollPane to wrap the JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Add the JComboBox and the JScrollPane to the panel
        panel.add(fileComboBox, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add an ActionListener to the JComboBox to update the displayed content when a different file is selected
        fileComboBox.addActionListener(e -> {
            String fullFileName = filePath + fileComboBox.getSelectedIndex() + fileExt;
            displayContent(fullFileName, textArea);
        });

        return panel;
    }

    private void displayContent(String fileName, JTextArea textArea) {
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                textArea.read(reader, null);
            }
        } catch (IOException e) {
            textArea.setText("Error reading the file: " + fileName);
        }
    }


    public static double[][] inverse(double[][] matrix){
        int row = matrix.length, col = matrix[0].length;
        double [][] inverseMatrix = new double[row][col];
        double [][] minor;
        double [][] cofactor = new double [row ][col];
        double delta;
        int temp , temp1;


        for(temp = 0; temp < row; temp++) {
            for(temp1 = 0; temp1 < col; temp1++){
                minor = getMinor (matrix,temp, temp1);
                getDeterminant(minor);
                cofactor[temp][temp1] = Math.pow(-1.0, temp+temp1) * getDeterminant(minor);
            }
        }


        cofactor = transposing(cofactor);
        delta = getDeterminant(matrix);

        for(temp = 0; temp < row; temp++) {
            for(temp1 = 0; temp1 < col; temp1++) {
                inverseMatrix[temp][temp1] = cofactor[temp][temp1] / delta;
            }
        }
        return inverseMatrix;
    }

    private static double [][] getMinor (double[][] matrix, int i, int j) {
        // i for order in row
        //j for order in col
        int row = matrix.length, col = matrix[0].length;
        double [][] results = new double [row-1 ][col-1];
        int row_count = 0, col_count = 0;
        int temp, temp1;

        for(temp = 0; temp < row; temp++) {
            for(temp1 = 0; temp1 < col; temp1++) {
                if(temp != i && temp1 != j) {
                    results[row_count][col_count] = matrix[temp][temp1];
                    col_count++;
                }
            }//end col loop
            col_count = 0;
            if(i != temp)
                row_count++;
        }//end row loop
        return results;
    }

    private static double [][] transposing (double [][] matrix) {
        double [][] transportMatrix = new double[matrix[0].length][matrix.length];
        int temp1, temp;
        for(temp = 0 ; temp < matrix[0].length; temp++) {
            for(temp1 = 0; temp1 < matrix.length; temp1++) {
                transportMatrix[temp][temp1]  =
                        matrix[temp1][temp]; //swap (temp, temp1)
            }
        }
        return transportMatrix;
    }

    private static double getDeterminant (double [][] matrix) {
        int temp, temp1, temp2;
        double coeficient;
        double result = 1;
        int sign = 1;
        int zeroCounter ;

        double[][] res = new double [matrix.length][matrix[0].length];

        //copy matrix
        for(temp = 0; temp < matrix.length; temp++)
        {
            for(temp1 = 0; temp1 < matrix[0].length; temp1++)
            {
                res[temp][temp1] = matrix[temp][temp1];
            }

        }

        //rearrange rows
        for(temp = 0; temp < res.length; temp++) {
            if(res[temp][temp] != 0)
                continue;
            for(temp1 = 1; temp1 < res.length -1 ; temp1++) {
                if( res[ (temp1 + temp ) % matrix.length][temp] != 0) {
                    swap(res[temp], res[(temp1 + temp ) % res.length]);
                    sign *= -1;
                    break;
                }
            }
        }
        for(temp = 1; temp < res.length; temp++) {
            for(temp1 = 0; temp1 < temp; temp1++){
                if(res[temp][temp1] == 0 || res[temp1][temp1] == 0)
                    continue;
                else {
                    zeroCounter = 0;
                    coeficient = res[temp][temp1]/res[temp1][temp1];
                }
                for(temp2 = 0; temp2 < res.length; temp2++) {
                    res[temp][temp2] = res[temp][temp2]
                            - res[temp1][temp2] * coeficient;

                    if(res[temp][temp2] == 0)
                        zeroCounter++;
                }
                if(temp < res.length -1 && zeroCounter > temp) {
                    swap(res[temp], res[temp+1]);
                    sign *= -1;
                    temp--;
                }
            }
        }
        for(temp = 0; temp < res.length; temp++) {
            result *= res[temp][temp];
        }
        return result * sign;
    }

    private static double [][] multiplyByScalar (double [][] matrix , double x) {
        double[][] results = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                results[i][j] = x * matrix[i][j];
            }
        }
        return results;
    }

    static double[][] newMatrix(){
        JTextField rField = new JTextField(5);
        JTextField cField = new JTextField(5); 

        JPanel[] choosePanel = new JPanel [2];
        choosePanel [0] = new JPanel();
        choosePanel [1] = new JPanel();
        choosePanel[0].add(new JLabel("Enter Dimension") );
        choosePanel[1].add(new JLabel("Rows:"));
        choosePanel[1].add(rField);
        choosePanel[1].add(Box.createHorizontalStrut(15));
        choosePanel[1].add(new JLabel("Cols:"));
        choosePanel[1].add(cField);
        int result = JOptionPane.showConfirmDialog(null, choosePanel,
                null, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != 0) return null;
        else {
            try {
                int row = Integer.parseInt(rField.getText());
                int col = Integer.parseInt(cField.getText());
                if(col < 1 || row < 1){
                    JOptionPane.showMessageDialog(null, "Wrong Dimensions");
                    return null;
                }else if (col > 100 || row > 100) {
                    JOptionPane.showMessageDialog(null, "Dimensions are too big");
                    return null;
                }else{
                    return setElements(new double[row][col]);
                }
                
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, "Wrong Dimensions");
                return null;
            }
                     
        }
        
    }
    
    static double[][] setElements(double[][] matrix) {
        int row = matrix.length, col = matrix[0].length;
        double[][] returningMatrix = new double[row][col];
        JPanel[] choosePanel = new JPanel[row + 2];
        choosePanel[0] = new JPanel();
        choosePanel[0].add(new Label("Enter the elements"));
        choosePanel[choosePanel.length - 1] = new JPanel();
        choosePanel[choosePanel.length - 1].add(new Label("Empty fields will be considered as 0"));
        JTextField[][] inputField = new JTextField[row][col];

        for (int i = 1; i <= row; i++) {
            choosePanel[i] = new JPanel();
            for (int j = 0; j < col; j++) {
                inputField[i - 1][j] = new JTextField(3);
                choosePanel[i].add(inputField[i - 1][j]);
                if (j < col - 1) choosePanel[i].add(Box.createHorizontalStrut(15));
            }
        }

        int result = JOptionPane.showConfirmDialog(null, choosePanel,
                null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != 0) return null;
        else {
            try {
                for (int i = 0; i < row; i++) {
                    for (int j = 0; j < col; j++) {
                        if (inputField[i][j].getText().isEmpty()) continue;
                        returningMatrix[i][j] = Double.parseDouble(inputField[i][j].getText());
                    }
                }
                return returningMatrix;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "You entered wrong elements");
                return null;
            }
        }
    }

    static void showMatrix(JPanel panel, double[][] matrix) {
        panel.removeAll(); // Remove previous components, if any

        // Create a custom panel to display the matrix as a grid
        JPanel matrixPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int rows = matrix.length;
                int cols = matrix[0].length;
                int cellSize = 50;
                int startX = 20;
                int startY = 20;

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        String value = String.format("%.2f", matrix[i][j]);

                        // Draw the cell rectangle
                        g.setColor(Color.WHITE);
                        g.fillRect(startX + j * cellSize, startY + i * cellSize, cellSize, cellSize);
                        g.setColor(Color.BLACK);
                        g.drawRect(startX + j * cellSize, startY + i * cellSize, cellSize, cellSize);

                        // Draw the value at the center of the cell
                        FontMetrics metrics = g.getFontMetrics();
                        int textWidth = metrics.stringWidth(value);
                        int textHeight = metrics.getHeight();
                        int textX = startX + j * cellSize + (cellSize - textWidth) / 2;
                        int textY = startY + i * cellSize + (cellSize - textHeight) / 2 + metrics.getAscent();
                        g.drawString(value, textX, textY);
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                int cellSize = 50;
                int width = matrix[0].length * cellSize + 2 * 20;
                int height = matrix.length * cellSize + 2 * 20;
                return new Dimension(width, height);
            }
        };

        matrixPanel.setBackground(Color.WHITE);
        matrixPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Wrap the matrixPanel inside a JScrollPane
        JScrollPane scrollPane = new JScrollPane(matrixPanel);
        scrollPane.setPreferredSize(new Dimension(700, 450));

        panel.add(scrollPane); // Add the JScrollPane to the parent panel

        panel.revalidate();
        panel.repaint();
    }
    static void showMatrix(double [][] matrix) {
        JPanel[] choosePanel = new JPanel[matrix.length + 1];
        choosePanel[0] = new JPanel();
        choosePanel[0].add(new JLabel(""));
        for (int i = 1; i <= matrix.length; i++) {
            choosePanel[i] = new JPanel();
            for (int j = 0; j < matrix[0].length; j++) {
                choosePanel[i].add(new JLabel(String.format("%.2f", matrix[i - 1][j])));
                if (j < matrix[0].length - 1)
                    choosePanel[i].add(Box.createHorizontalStrut(15));
            }
        }
        JOptionPane.showMessageDialog(null, choosePanel, null, JOptionPane.PLAIN_MESSAGE, null);
    }

    private double[][] matrixAdditionSubstruction(double[][] matrixA, double[][] matrixB, int index) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int rowsB = matrixB.length;
        int colsB = matrixB[0].length;
        if (rowsA != rowsB || colsA != colsB) {
            return null; // Matrices are not compatible for addition
        }
        double[][] result = new double[rowsA][colsA];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i][j] = matrixA[i][j] + Math.pow(-1, index)*matrixB[i][j];
            }
        }
        return result;
    }

    private static void swap (double [] res1, double [] res2)
    {
        int temp;
        double tempDouble;

        for(temp = 0; temp < res1.length;temp++)
        {
            tempDouble = res1[temp];
            res1[temp] = res2[temp];
            res2[temp] = tempDouble;
        }
    }
}