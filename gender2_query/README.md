### How to run this script
1. With Python 2 installed, install the libraries:
   ```
   pip install requests
   ```

2. Select the distinct first names from BigQuery, with the format a first name a line, save the names in the file: names2query.csv.
   Select Example:
   ```
   SELECT first_name, country_code
   FROM
      (SELECT inventor_name_first first_name, lower(country) country_code
      FROM[natural-venture-179401:Leaderboard.decades_cleaned]),
      (SELECT inventor_name_first first_name, lower(inventor_country_code) country_code
      FROM[natural-venture-179401:Leaderboard.non_US_all_cleaned_gender2]),
      (SELECT inventor_name_first first_name, lower(inventor_country_code) country_code
      FROM[natural-venture-179401:Leaderboard.non_US_AI_cleaned_gender2]),
      (SELECT inventor_name_first first_name, lower(inventor_country_code) country_code
      FROM[natural-venture-179401:Leaderboard.US_AI_cleaned_gender2]),
      (SELECT inventor_name_first first_name, lower(inventor_country_code) country_code
      FROM[natural-venture-179401:Leaderboard.US_all_cleaned_gender2]),
      (SELECT inventor_name_first first_name, '' country_code
      FROM[natural-venture-179401:Leaderboard.decades_cleaned]),
      (SELECT inventor_name_first first_name, '' country_code
      FROM[natural-venture-179401:Leaderboard.non_US_all_cleaned_gender2]),
      (SELECT inventor_name_first first_name, '' country_code
      FROM[natural-venture-179401:Leaderboard.non_US_AI_cleaned_gender2]),
      (SELECT inventor_name_first first_name, '' country_code
      FROM[natural-venture-179401:Leaderboard.US_AI_cleaned_gender2]),
      (SELECT inventor_name_first first_name, '' country_code
      FROM[natural-venture-179401:Leaderboard.US_all_cleaned_gender2]),
      (SELECT string_field_2 first_name, lower(string_field_1) country_code
      FROM[my-project-1491577527670:patents.inventor_2015_2018_2]),
      (SELECT string_field_2 first_name, '' country_code
      FROM[my-project-1491577527670:patents.inventor_2015_2018_2]),
      (SELECT f_name first_name, '' country_code
      FROM [natural-venture-179401:Leaderboard.aaai_cleaned]),
      (SELECT author_name_first first_name, '' country_code
      FROM [natural-venture-179401:Leaderboard.nips_cleaned])
   GROUP BY first_name, country_code
   ```

   If you want to use the previous queried data, please keep the data in "gender_tmp.csv"; otherwise, have better to backup old "gender_tmp.csv" for future comparison, then clear the data in "gender_tmp.csv";

3. Run the script:
   ```
   python run.py
   ```

4. Repeat step 3 until get all the names results. From the terminal, you can see how many names to be queried. And from "fail.txt", you can check which names failed to query last time. Repeat step 3 unitl # names to be queried is 0.

5. Upload the result data "gender_tmp.csv" into BigQuery, and execute the SQL to merge the results, then save the merge results into a new table with suffix gender2.
   Select example:
   ```
   #standardSQL
   SELECT a.*, b.gender, b.probability, b.count
   FROM `natural-venture-179401.Leaderboard.nips_cleaned` a
   LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_2` b
   ON a.author_name_first = b.name_first;
   ```
