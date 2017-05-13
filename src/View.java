import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Comanche-like simple voxel space engine Test
 * @author leonardo
 */
public class View extends JPanel {
    
    private double px = 800, py = 500, pd = 1.7; // view position = (px, py) / view direction = pd (angle in rad)
    private BufferedImage offscreen, heightMap, textureMap;
    private final Color skyColor = new Color(150, 170, 170);

    public View() {
        offscreen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        try {
            heightMap = ImageIO.read(getClass().getResourceAsStream("mountains.jpg"));
            textureMap = ImageIO.read(getClass().getResourceAsStream("mountains3.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(offscreen.createGraphics());
        g.drawImage(offscreen, 0, 0, 800, 600, 0, 0, 400, 300, null);
        // animation / move view position 
        px += 2 * Math.cos(pd);
        py += 2 * Math.sin(pd);
        pd += 0.01;
        repaint();
    }
    
    private void draw(Graphics2D g) {
        g.setBackground(skyColor);
        g.clearRect(0, 0, getWidth(), getHeight());
        // cast rays
        int sx = 0;
        for (double angle = -0.5; angle < 1; angle += 0.0035) {
            int maxScreenHeight = getHeight();
            double s = Math.cos(pd + angle);
            double c = Math.sin(pd + angle);
            for (int depth = 10; depth < 600; depth += 1) {
                int hmx = (int) (px + depth * s);
                int hmy = (int) (py + depth * c);
                if (hmx < 0 || hmy < 0 || hmx > heightMap.getWidth() - 1 || hmy > heightMap.getHeight() - 1) {
                    continue;
                }
                int height = heightMap.getRGB(hmx, hmy) & 255;
                int color = addFog(textureMap.getRGB(hmx, hmy), depth);
                
                // draw 3D vertical terrain line / circular projection
                double sy = 120 * (300 - height) / depth; 
                if (sy > maxScreenHeight) {
                    continue;
                }
                for (int y = (int) sy; y <= maxScreenHeight; y++) {
                    if (y < 0 || sx > offscreen.getWidth() - 1 || y > offscreen.getHeight() - 1) {
                        continue;
                    }
                    offscreen.setRGB(sx, y, color);
                }
                maxScreenHeight = (int) sy;
            }
            sx++;
        }
    }
    
    private int addFog(int color, int depth) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        double p = depth > 100 ? (depth - 100) / 500.0 : 0;
        r = (int) (r + (skyColor.getRed() - r) * p);
        g = (int) (g + (skyColor.getGreen() - g) * p);
        b = (int) (b + (skyColor.getBlue() - b) * p);
        return (r << 16) + (g << 8) + b;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setTitle("Java Simple Voxel Space Engine Test");
                frame.getContentPane().add(new View());
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                frame.setVisible(true);
            }
        });
    }
    
}
