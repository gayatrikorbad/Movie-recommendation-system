package movieSystemmoviesystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataCollection {
    public static void main(String[] args) {
        // Specify the path to your dataset file
        String datasetPath = "C:\\Users\\HP\\Desktop\\MyProject\\ratings.csv";

        // Call a method to read the dataset and collect movie ratings
        List<MovieRating> movieRatings = collectMovieRatings(datasetPath);

        // Display the collected movie ratings
        for (MovieRating rating : movieRatings) {
            System.out.println("User: " + rating.getUserId() +
                    ", Movie: " + rating.getMovieId() +
                    ", Rating: " + rating.getRating());
        }
    }

    //
    private static List<MovieRating> collectMovieRatings(String datasetPath) {
        List<MovieRating> movieRatings = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(datasetPath))) {
            br.readLine(); // Skip the header row if it exists

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 3) {
                    System.err.println("Invalid data format: " + line);
                    continue;
                }
                try {
                    int userId = Integer.parseInt(data[0]);
                    int movieId = Integer.parseInt(data[1]);
                    double rating = Double.parseDouble(data[2]);

                    MovieRating movieRating = new MovieRating(userId, movieId, rating);
                    movieRatings.add(movieRating);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid numeric value: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieRatings;
    }

    static class MovieRating {
        private int userId;
        private int movieId;
        private double rating;

        public MovieRating(int userId, int movieId, double rating) {
            this.userId = userId;
            this.movieId = movieId;
            this.rating = rating;
        }

        public int getUserId() {
            return userId;
        }

        public int getMovieId() {
            return movieId;
        }

        public double getRating() {
            return rating;
        }
    }
}