import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Core extends JFrame {

    private final int onePlateSize = 100;
    private final int gameBoardSideSize = 8;
    private final JPanel gamePanel;
    private final Map<String, Image> pieceAndImages;
    private final String[][] board;
    private Point selectedPiece;
    private boolean gameStarted;
    private Point animFrom, animTo;
    private double animationProgress;
    private boolean isWhiteTurn;
    private final JLabel turnLabel;

    public Core() {
        pieceAndImages = new HashMap<>();
        loadPieceAndImages();

        board = new String[gameBoardSideSize][gameBoardSideSize];
        initializeBoard();

        
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                
                super.paintComponent(g);
                drawBoard(g);
                if (gameStarted) {
                    drawPieces(g);
                }
            }
        };

        int windowSize = onePlateSize * gameBoardSideSize;
            
        gamePanel.setPreferredSize(new Dimension(windowSize,windowSize));
        
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameStarted) {
                    handleMouseClick(e);
                }
            }
        });


        JPanel controlPanel = new JPanel();

        JButton startButton = new JButton("Start Game");
        JButton loadButton = new JButton("Load Game");
        JButton saveButton = new JButton("Save Game");

        startButton.addActionListener(e -> startGame());
        saveButton.addActionListener(e -> saveGame());
        loadButton.addActionListener(e -> loadGame());

        controlPanel.add(startButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        turnLabel = new JLabel("Turn: White");
        controlPanel.add(turnLabel);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);

        setTitle("blyblybly");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gameStarted = false;
        isWhiteTurn = true;
    }

    private void loadPieceAndImages() {
        String[] pieces = {"Bialy_krol", "Biala_krolowa", "Biala_wieza", "Bialy_skoczek", "Bialy_kon", "Bialy_pionek",
                "Czarny_krol", "Czarna_krolowa", "Czarna_wieza", "Czarny_skoczek", "Czarny_kon", "Czarny_pionek"};
        for (String piece : pieces) {
            
            pieceAndImages.put(piece, new ImageIcon(getClass().getResource("/pieces/" + piece + ".png")).getImage());
        }
    }

    private void initializeBoard() {
        String[] backRow = {"Czarna_wieza", "Czarny_kon", "Czarny_skoczek", "Czarna_krolowa", "Czarny_krol", "Czarny_skoczek", "Czarny_kon", "Czarna_wieza"};
        String[] pawnRow = {"Czarny_pionek", "Czarny_pionek", "Czarny_pionek", "Czarny_pionek", "Czarny_pionek", "Czarny_pionek", "Czarny_pionek", "Czarny_pionek"};
        String[] backRow1 = {"Biala_wieza", "Bialy_kon", "Bialy_skoczek", "Biala_krolowa", "Bialy_krol", "Bialy_skoczek", "Bialy_kon", "Biala_wieza"};
        String[] pawnRow1 = {"Bialy_pionek", "Bialy_pionek", "Bialy_pionek", "Bialy_pionek", "Bialy_pionek", "Bialy_pionek", "Bialy_pionek", "Bialy_pionek"};
        String[] emptyRow = {"", "", "", "", "", "", "", ""};
        board[0] = backRow;
        board[1] = pawnRow;
        for (int i = 2; i < 6; i++) {
            board[i] = emptyRow.clone();
        }
        board[6] = pawnRow1;
        board[7] = backRow1;
    }

    private void drawBoard(Graphics g) {
        for (int i = 0; i < gameBoardSideSize; i++) {
            for (int col = 0; col < gameBoardSideSize; col++) {
                if ((i + col) % 2 == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.GRAY);
                }
                g.fillRect(col * onePlateSize, i * onePlateSize, onePlateSize, onePlateSize);
            }
        }
    }

    private void drawPieces(Graphics g) {
        for (int i = 0; i < gameBoardSideSize; i++) {
            for (int j = 0; j < gameBoardSideSize; j++) {
                String piece = board[i][j];
                if (!piece.isEmpty()) {
                    g.drawImage(pieceAndImages.get(piece), j * onePlateSize, i * onePlateSize, onePlateSize, onePlateSize, this);
                }
            }
        }
    }

    private boolean isItThePossibleMove(String piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (piece.startsWith("B") && board[toRow][toCol].startsWith("B") ||
                piece.startsWith("C") && board[toRow][toCol].startsWith("C")) {
            return false;
        }
        switch (piece) {
            case "Bialy_pionek":
                if (fromRow == 6 && toRow == fromRow - 2 && fromCol == toCol && board[toRow][toCol].isEmpty()) {
                    return true;
                }
                if (toRow == fromRow - 1 && fromCol == toCol && board[toRow][toCol].isEmpty()) {
                    return true;
                }
                if (toRow == fromRow - 1 && Math.abs(fromCol - toCol) == 1 && !board[toRow][toCol].isEmpty()) {
                    return true;
                }
                break;

            case "Czarny_pionek":
                if (fromRow == 1 && toRow == fromRow + 2 && fromCol == toCol && board[toRow][toCol].isEmpty()) {
                    return true;
                }
                if (toRow == fromRow + 1 && fromCol == toCol && board[toRow][toCol].isEmpty()) {
                    return true;
                }
                if (toRow == fromRow + 1 && Math.abs(fromCol - toCol) == 1 && !board[toRow][toCol].isEmpty()) {
                    return true;
                }
                break;
            case "Bialy_krol":
            case "Czarny_krol":
                if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1) {
                    return true;
                }
                break;
            case "Biala_wieza":
            case "Czarna_wieza":
                if (fromRow == toRow) {
                    int step = (toCol > fromCol) ? 1 : -1;
                    for (int col = fromCol + step; col != toCol; col += step) {
                        if (!board[fromRow][col].isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                } else if (fromCol == toCol) {
                    int step = (toRow > fromRow) ? 1 : -1;
                    for (int row = fromRow + step; row != toRow; row += step) {
                        if (!board[row][fromCol].isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                }
                break;
            case "Bialy_skoczek":
            case "Czarny_skoczek":
                if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                    int rowStep = (toRow > fromRow) ? 1 : -1;
                    int colStep = (toCol > fromCol) ? 1 : -1;
                    int row = fromRow + rowStep;
                    int col = fromCol + colStep;
                    while (row != toRow && col != toCol) {
                        if (!board[row][col].isEmpty()) {
                            return false;
                        }
                        row += rowStep;
                        col += colStep;
                    }
                    return true;
                }
                break;
            case "Biala_krolowa":
            case "Czarna_krolowa":
                if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                    int rowStep = (toRow > fromRow) ? 1 : -1;
                    int colStep = (toCol > fromCol) ? 1 : -1;
                    int row = fromRow + rowStep;
                    int col = fromCol + colStep;
                    while (row != toRow && col != toCol) {
                        if (!board[row][col].isEmpty()) {
                            return false;
                        }
                        row += rowStep;
                        col += colStep;
                    }
                    return true;
                }
                if (fromRow == toRow) {
                    int step = (toCol > fromCol) ? 1 : -1;
                    for (int col = fromCol + step; col != toCol; col += step) {
                        if (!board[fromRow][col].isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                } else if (fromCol == toCol) {
                    int step = (toRow > fromRow) ? 1 : -1;
                    for (int row = fromRow + step; row != toRow; row += step) {
                        if (!board[row][fromCol].isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                }
                break;
            case "Czarny_kon":
            case "Bialy_kon":
                if ((Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 1) ||
                        (Math.abs(fromRow - toRow) == 1 && Math.abs(fromCol - toCol) == 2)) {
                    return true;
                }
                break;
        }
        return false;
    }

    private void handleMouseClick(MouseEvent e) {
        int col = e.getX() / onePlateSize;
        int row = e.getY() / onePlateSize;

        if (selectedPiece == null) {
            if (!board[row][col].isEmpty()) {
                String piece = board[row][col];
                if ((isWhiteTurn && piece.startsWith("B")) || (!isWhiteTurn && piece.startsWith("C"))) {
                    selectedPiece = new Point(col, row);
                }
            }
        } else {
            int fromCol = selectedPiece.x;
            int fromRow = selectedPiece.y;
            String piece = board[fromRow][fromCol];
            if (isItThePossibleMove(piece, fromRow, fromCol, row, col)) {
                board[row][col] = board[fromRow][fromCol];
                board[fromRow][fromCol] = "";
                selectedPiece = null;
                gamePanel.repaint();
                checkWin();
                animFrom = new Point(fromCol, fromRow);
                animTo = new Point(col, row);
                animationProgress = 0;
                startAnimation();
                isWhiteTurn = !isWhiteTurn;
                updateTurnLabel();
                System.out.println(piece + " from " + fromRow + "," + fromCol + " to " + row + "," + col);
            } else {
                selectedPiece = null;
            }
        }
    }

    private void startAnimation() {
        new Thread(() -> {
            try {
                while (animationProgress < 1) {
                    animationProgress += 0.0001;
                    gamePanel.repaint();
                    Thread.sleep(100);
                }
                animFrom = null;
                animTo = null;
                animationProgress = 0;
                gamePanel.repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void checkWin() {
        boolean whiteKingStillInGamee = false;
        boolean blackKingStillInGame = false;

        for (int i = 0; i < gameBoardSideSize; i++) {
            for (int j = 0; j < gameBoardSideSize; j++) {
                if (board[i][j].equals("Bialy_krol")) {
                    whiteKingStillInGamee = true;
                }
                if (board[i][j].equals("Czarny_krol")) {
                    blackKingStillInGame = true;
                }
            }
        }

        if (!whiteKingStillInGamee) {
            gameStarted = false;
            JOptionPane.showMessageDialog(this, "Czarne pionki zwyciężyły!");
        } else if (!blackKingStillInGame) {
            gameStarted = false;
            JOptionPane.showMessageDialog(this, "Biale pionki zwyciężyły!");
        }
    }

    private void saveGame() {
        try (FileWriter fw = new FileWriter("chessGameSaverAndLoader.txt")) {
            for (int i = 0; i < gameBoardSideSize; i++) {
                for (int j = 0; j < gameBoardSideSize; j++) {
                    fw.write((board[i][j].isEmpty() ? "empty" : board[i][j]) + " ");
                }
                fw.write("\n");
            }
            fw.write(isWhiteTurn ? "White" : "Black");
//            System.out.println("udalo sie zapisac");
            JOptionPane.showMessageDialog(this, "Game saved successfully.");
        } catch (IOException e) {
//            System.out.println(" nie udalo sie zapisac");
            JOptionPane.showMessageDialog(this, "Failed to save the game.");
        }
    }

    private void loadGame() {
        try (Scanner sc = new Scanner(new File("chessGameSaverAndLoader.txt"))) {
            for (int i = 0; i < gameBoardSideSize; i++) {
                for (int j = 0; j < gameBoardSideSize; j++) {
                    if (sc.hasNext()) {
                        String piece = sc.next();
                        board[i][j] = piece.equals("empty") ? "" : piece;
                    }
                }
            }
            if (sc.hasNext()) {
                String turn = sc.next();
                isWhiteTurn = turn.equals("White");
            }
            updateTurnLabel();
            gameStarted = true;
            gamePanel.repaint();
//            System.out.println("udalo sie wczytac");
            JOptionPane.showMessageDialog(this, "Game loaded successfully.");
        } catch (FileNotFoundException e) {
//            System.out.println(" nie udalo sie wczytac");
            JOptionPane.showMessageDialog(this, "Failed to load the game.");

        }
    }

    private void updateTurnLabel() {
        if (isWhiteTurn) {
            turnLabel.setText("Turn: White");
        } else {
            turnLabel.setText("Turn: The dark side of the force");
        }
    }

    private void startGame() {
        isWhiteTurn = true;
        updateTurnLabel();
        gameStarted = true;
        initializeBoard();
        gamePanel.repaint();
    }
}