package movieSystemmoviesystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UserItemMatrix extends DataCollection {

    public static void main(String[] args) {

        List<MovieRating> movieRatings = collectMovieRatings(); // Collect movie ratings from users

        // Build the user-item matrix
        Map<Integer, Map<Integer, Double>> userItemMatrix = buildUserItemMatrix(movieRatings);

        Map<Integer, Map<Integer, Double>> userSimilarityMatrix = calculateUserSimilarity(userItemMatrix);

        Map<Integer, Map<Integer, Double>> itemSimilarityMatrix = calculateItemSimilarity(userItemMatrix);

        List<Integer> recomondation = getTopRecommendedMovies(2, userSimilarityMatrix, userItemMatrix);

        // Display the user-item matrix
        // for (int userId : userItemMatrix.keySet()) {
        // Map<Integer, Double> ratings = userItemMatrix.get(userId);
        // System.out.println("User: " + userId + ", Ratings: " + ratings);
        // }
        System.out.print(recomondation);

    }

    private static List<MovieRating> collectMovieRatings() {

        String datasetPath = "C:\\Users\\Admin\\Desktop\\movielens\\ratings.csv";

        // Call a method to read the dataset and collect movie ratings
        List<MovieRating> movieRatings = collectMovieRatings(datasetPath);

        // Collect movie ratings from users
        // Example: Manually adding ratings for demonstration purposes
        // movieRatings.add(new MovieRating(1, 101, 4.5));
        // movieRatings.add(new MovieRating(1, 102, 3.0));
        // movieRatings.add(new MovieRating(2, 101, 2.5));
        // movieRatings.add(new MovieRating(2, 103, 4.0));
        // // ...
        //
        return movieRatings;
    }

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

    private static Map<Integer, Map<Integer, Double>> buildUserItemMatrix(List<MovieRating> movieRatings) {
        Map<Integer, Map<Integer, Double>> userItemMatrix = new HashMap<>();

        for (MovieRating rating : movieRatings) {
            int userId = rating.getUserId();
            int movieId = rating.getMovieId();
            double ratingValue = rating.getRating();

            // Get the user's rating map from the userItemMatrix
            Map<Integer, Double> userRatings = userItemMatrix.getOrDefault(userId, new HashMap<>());

            // Add the movie rating to the user's rating map
            userRatings.put(movieId, ratingValue);

            // Put the updated user rating map back into the userItemMatrix
            userItemMatrix.put(userId, userRatings);
        }

        return userItemMatrix;
    }

    private static Map<Integer, Map<Integer, Double>> calculateUserSimilarity(
            Map<Integer, Map<Integer, Double>> userItemMatrix) {

        Map<Integer, Map<Integer, Double>> userSimilarityMatrix = new HashMap<>();

        for (int userId1 : userItemMatrix.keySet()) {
            Map<Integer, Double> similarityMap = new HashMap<>();

            for (int userId2 : userItemMatrix.keySet()) {
                if (userId1 == userId2) {
                    continue; // Skip calculating similarity with the same user
                }

                double similarity = calculateCosineSimilarity(userItemMatrix, userId1, userId2);

                // Store the similarity between userId1 and userId2
                similarityMap.put(userId2, similarity);
            }

            // Add the similarity map to the user similarity matrix
            userSimilarityMatrix.put(userId1, similarityMap);
        }

        return userSimilarityMatrix;
    }

    private static Map<Integer, Map<Integer, Double>> calculateItemSimilarity(
            Map<Integer, Map<Integer, Double>> userItemMatrix) {

        Map<Integer, Map<Integer, Double>> itemSimilarityMatrix = new HashMap<>();

        for (int movieId1 : userItemMatrix.get(1).keySet()) {
            Map<Integer, Double> similarityMap = new HashMap<>();

            for (int movieId2 : userItemMatrix.get(1).keySet()) {
                if (movieId1 == movieId2) {
                    continue; // Skip calculating similarity with the same movie
                }

                double similarity = calculateCosineSimilarity(userItemMatrix, movieId1, movieId2);

                // Store the similarity between movieId1 and movieId2
                similarityMap.put(movieId2, similarity);
            }

            // Add the similarity map to the item similarity matrix
            itemSimilarityMatrix.put(movieId1, similarityMap);
        }

        return itemSimilarityMatrix;
    }

    private static double calculateCosineSimilarity(Map<Integer, Map<Integer, Double>> userItemMatrix, int movieId1,
            int movieId2) {
        Map<Integer, Double> ratings1 = new HashMap<>();
        Map<Integer, Double> ratings2 = new HashMap<>();

        // Collect ratings of movieId1 and movieId2 from all users
        for (Map<Integer, Double> ratings : userItemMatrix.values()) {
            if (ratings.containsKey(movieId1)) {
                ratings1.put(ratings.hashCode(), ratings.get(movieId1));
            }
            if (ratings.containsKey(movieId2)) {
                ratings2.put(ratings.hashCode(), ratings.get(movieId2));
            }
        }

        // Calculate the dot product of the rating vectors
        double dotProduct = 0.0;
        for (int userId : ratings1.keySet()) {
            if (ratings2.containsKey(userId)) {
                dotProduct += ratings1.get(userId) * ratings2.get(userId);
            }
        }

        // Calculate the magnitude of the rating vectors
        double magnitude1 = calculateVectorMagnitude(ratings1);
        double magnitude2 = calculateVectorMagnitude(ratings2);

        // Calculate the cosine similarity
        double cosineSimilarity = dotProduct / (magnitude1 * magnitude2);

        return cosineSimilarity;
    }

    private static double calculateVectorMagnitude(Map<Integer, Double> vector) {
        double magnitude = 0.0;
        for (double value : vector.values()) {
            magnitude += Math.pow(value, 2);
        }
        return Math.sqrt(magnitude);
    }

    private static List<Integer> getTopRecommendedMovies(int targetUserId,
            Map<Integer, Map<Integer, Double>> userSimilarityMatrix,
            Map<Integer, Map<Integer, Double>> userItemMatrix) {

        if (!userItemMatrix.containsKey(targetUserId)) {
            System.out.println("User " + targetUserId + " does not exist in the userItemMatrix.");
            return new ArrayList<>();
        }

        Map<Integer, Double> targetUserRatings = userItemMatrix.get(targetUserId);
        Map<Integer, Double> movieScores = new HashMap<>();

        for (int movieId : targetUserRatings.keySet()) {
            for (int userId : userSimilarityMatrix.keySet()) {
                if (userId == targetUserId) {
                    continue; // Skip calculating similarity with the same user
                }

                Map<Integer, Double> ratings = userItemMatrix.get(userId);
                if (ratings.containsKey(movieId)) {
                    double similarity = userSimilarityMatrix.get(targetUserId).get(userId);
                    double rating = ratings.get(movieId);
                    movieScores.put(movieId, movieScores.getOrDefault(movieId, 0.0) + rating * similarity);
                }
            }
        }

        List<Map.Entry<Integer, Double>> sortedScores = new ArrayList<>(movieScores.entrySet());
        sortedScores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<Integer> recommendedMovies = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : sortedScores) {
            recommendedMovies.add(entry.getKey());
        }

        return recommendedMovies;
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