/**
 * A simple game implemented using the Processing library.
 * The player controls a red box that can move horizontally to catch falling objects.
 * The goal is to collect stars for points while avoiding collisions with obstacles and a green circle.
 * The game ends when the player misses a falling object or collides with an obstacle.
 */
import javax.swing.JOptionPane;
import processing.core.PApplet;
import processing.core.PImage;

public class Sketch extends PApplet {
    // Position of the player's box
    private int boxX;

    // Position of the falling red fruit
    private int fruitX;
    private int fruitY;

    // Position of the falling star
    private int starX;
    private int starY;

    // Game state variables
    private boolean gameOver;
    private int score;

    // Falling speeds
    private float fallingSpeed;
    private float starSpeed;

    // Obstacle variables
    private int numObstacles = 10;
    private float[] obstacleX;
    private float[] obstacleY;
    private int obstacleSize = 30;
    private float spreadFactor = 0.5f;
    private int obstacleInterval = 120;
    private int frameCountSinceLastObstacle = 0;

    // Green circle variables
    private float greenCircleSpeed = 2.0f;
    private float greenCircleX;
    private float greenCircleY;
    private float greenCircleSize = 20;

    // Background image
    PImage backgroundImage;

      /**
   * Called once at the beginning of execution, put your size all in this method
   */
  public void settings() {
      size(400, 400);
    }

    /**
     * Sets up the initial conditions of the sketch.
     */
    public void setup() {
        // Load background image
        backgroundImage = loadImage("Piano Game BackGround.png");
        backgroundImage.resize(width, height);

        // Get falling speed from user input
        String speedInput = JOptionPane.showInputDialog("Enter falling speed (e.g., 2.0):");
        fallingSpeed = Float.parseFloat(speedInput);

        // Initialize player's box position
        boxX = width / 2;

        // Initialize positions of falling objects
        fruitX = (int) random(width);
        fruitY = 0;

        starX = (int) random(width);
        starY = -20;

        // Initialize game state
        gameOver = false;
        score = 0;
        starSpeed = 2.0f;

        // Initialize obstacle arrays
        obstacleX = new float[numObstacles];
        obstacleY = new float[numObstacles];

        // Initialize obstacles
        for (int i = 0; i < numObstacles; i++) {
            introduceNewObstacle(i);
        }

        // Initialize green circle
        respawnGreenCircle();
    }

    /**
     * Draws the current state of the sketch.
     */
    public void draw() {
        // Draw background image
        background(backgroundImage);

        if (!gameOver) {
            // Draw red fruit
            fill(255, 0, 0);
            ellipse(fruitX, fruitY, 20, 20);

            // Draw player's box
            fill(0, 0, 255);
            rect(boxX - 25, height - 30, 50, 20);

            // Draw green circle
            fill(0, 255, 0);
            ellipse(greenCircleX, greenCircleY, greenCircleSize, greenCircleSize);

            // Draw star
            fill(255, 255, 0);
            star(starX, starY, 10, 25, 5);

            // Introduce a new green circle at a regular interval
            frameCountSinceLastObstacle++;
            if (frameCountSinceLastObstacle >= obstacleInterval) {
                frameCountSinceLastObstacle = 0;
                respawnGreenCircle();
            }

            // Update and draw obstacles
            for (int i = 0; i < numObstacles; i++) {
                ellipse(obstacleX[i], obstacleY[i], obstacleSize, obstacleSize);
                obstacleY[i] += fallingSpeed;

                // Spread out the obstacles based on their vertical positions
                obstacleX[i] += spreadFactor * sin(obstacleY[i] * 0.1f);

                if (obstacleY[i] > height) {
                    introduceNewObstacle(i);
                }

                // Check for collision between red and green circles
                if (dist(fruitX, fruitY, obstacleX[i], obstacleY[i]) < 20) {
                    score--;  // Decrement score on collision
                    introduceNewObstacle(i);  // Respawn green circle
                }
            }

            // Move the green circle
            greenCircleY += greenCircleSpeed;

            // Respawn green circle if it reaches the bottom
            if (greenCircleY > height) {
                respawnGreenCircle();
            }

            // Check for collision between red and green circles
            if (dist(fruitX, fruitY, greenCircleX, greenCircleY) < 20) {
                score--;  // Decrement score on collision
                respawnGreenCircle();  // Respawn green circle
            }

            // Move the star
            starY += starSpeed;

            // Check for collision between box and star
            if (dist(boxX, height - 20, starX, starY) < 20) {
                score += 2;  // Increment score by 2 on collecting star
                respawnStar();  // Respawn star
            }

            // Respawn star if it reaches the bottom
            if (starY > height) {
                respawnStar();
            }

            // Update position of falling red fruit
            fruitY += fallingSpeed;

            // Increase falling speed over time
            fallingSpeed += 0.001;

            // Check for collision between fruit and player's box
            if (dist(fruitX, fruitY, boxX, height - 20) < 20) {
                fruitX = (int) random(width);
                fruitY = 0;
                score++;
            }

            // Check if fruit missed the player's box and reached the bottom
            if (fruitY > height) {
                gameOver = true;
            }

            // Check for collision between player's box and obstacles
            for (int i = 0; i < numObstacles; i++) {
                if (dist(boxX, height - 20, obstacleX[i], obstacleY[i]) < 20) {
                    gameOver = true;
                }
            }

            // Display the current score
            fill(0);
            textSize(16);
            textAlign(RIGHT, TOP);
            text("Score: " + score, width - 10, 10);
        } else {
            // Display game over message
            fill(255, 0, 0);
            textSize(32);
            textAlign(CENTER, CENTER);
            text("Game Over\nScore: " + score, width / 2, height / 2);
        }
    }

    /**
     * Introduces a new obstacle at a random position above the sketch window.
     * The obstacle should avoid collisions with the red fruit, green circle, and star.
     *
     * @param index The index of the obstacle in the obstacle arrays.
     */
    private void introduceNewObstacle(int index) {
        float minDistance = 50;

        // Find an available slot for a new obstacle, avoiding the red, green circles, and star
        do {
            obstacleX[index] = random(width);
            obstacleY[index] = -obstacleSize - random(height);
        } while (dist(obstacleX[index], obstacleY[index], fruitX, fruitY) < minDistance ||
                dist(obstacleX[index], obstacleY[index], greenCircleX, greenCircleY) < minDistance ||
                dist(obstacleX[index], obstacleY[index], starX, starY) < minDistance);
    }

    /**
     * Respawns the green circle at a random position above the sketch window.
     */
    private void respawnGreenCircle() {
        greenCircleX = random(width);
        greenCircleY = -greenCircleSize - random(height);
    }

    /**
     * Respawns the star at a random position above the sketch window.
     */
    private void respawnStar() {
        starX = (int) random(width);
        starY = -20;
    }

    /**
     * Handles user input for moving the player's box left or right.
     */
    public void keyPressed() {
        if (!gameOver) {
            if (keyCode == LEFT && boxX > 25) {
                boxX -= 10;
            } else if (keyCode == RIGHT && boxX < width - 25) {
                boxX += 10;
            }
        }
    }

    /**
     * Draws a star shape at the specified position with the given radii and number of points.
     *
     * @param x        The x-coordinate of the center of the star.
     * @param y        The y-coordinate of the center of the star.
     * @param radius1  The outer radius of the star points.
     * @param radius2  The inner radius of the star points.
     * @param npoints  The number of points of the star.
     */
    private void star(float x, float y, float radius1, float radius2, int npoints) {
        float angle = TWO_PI / npoints;
        float halfAngle = angle / 2;
        beginShape();
        for (float a = -PI / 2; a < TWO_PI - PI / 2; a += angle) {
            float sx = x + cos(a) * radius2;
            float sy = y + sin(a) * radius2;
            vertex(sx, sy);
            sx = x + cos(a + halfAngle) * radius1;
            sy = y + sin(a + halfAngle) * radius1;
            vertex(sx, sy);
        }
        endShape(CLOSE);
    }

    /**
     * The main entry point for the sketch.
     *
     * @param args Command line arguments (not used in this sketch).
     */
    public static void main(String[] args) {
        PApplet.main("Sketch");
    }
}
