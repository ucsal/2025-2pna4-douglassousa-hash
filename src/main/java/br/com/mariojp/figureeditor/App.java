package br.com.mariojp.figureeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("Figure Editor — Clique para inserir figuras");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            DrawingPanel panel = new DrawingPanel();
            JToolBar toolbar = new JToolBar();
            JButton colorBtn = new JButton("Cor...");
            colorBtn.addActionListener(e -> {
                Color c = JColorChooser.showDialog(frame, "Escolha uma cor", panel.getCurrentColor());
                if (c != null) panel.setCurrentColor(c);
            });
            toolbar.add(colorBtn);
            
            JButton upBtn = new JButton("Subir camada");
            upBtn.addActionListener(e -> panel.bringToFront());
            toolbar.add(upBtn);

            JButton downBtn = new JButton("Descer camada");
            downBtn.addActionListener(e -> panel.sendToBack());
            toolbar.add(downBtn);
            
            JButton exportBtn = new JButton("Exportar PNG");
            exportBtn.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        panel.exportPNG(chooser.getSelectedFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Erro ao salvar: " + ex.getMessage());
                    }
                }
            });
            toolbar.add(exportBtn);
            
            JButton undoBtn = new JButton("Undo");
            undoBtn.addActionListener(e -> panel.undo());
            toolbar.add(undoBtn);

            JButton redoBtn = new JButton("Redo");
            redoBtn.addActionListener(e -> panel.redo());
            toolbar.add(redoBtn);
            
            
            frame.add(toolbar, BorderLayout.NORTH);
            

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
