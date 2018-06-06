## SQL scripts to combine ssn / wipo / genderize.io / GenderAPI gender classifer

ssn source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.gender_classifier_1
wipo source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.wipo_classifier
(These two use no country code)
genderize.io source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.gender_classifier_2
GenderAPI source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.genderapi_classifier
(These two use country code)

### SQL Scripts
1. Select the distinct (first names, country_code) from BigQuery, with the format a first name a line, save the result into local dataset.
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

2. Append ssn data: adjust table `natural-venture-179401.Leaderboard.gender_classifier_1`, save to local, then append the data.
   ```
   #standardSQL
   SELECT lower(name) name, CASE WHEN a.ratio > 0.5 THEN 'female' ELSE 'male' END AS gender, CASE WHEN a.ratio > 0.5 THEN a.ratio ELSE 1 - a.ratio END AS probability, total_num count
   From `natural-venture-179401.Leaderboard.gender_classifier_1` a
   ```
   Save the result above into local dataset `my-project-1491577527670.gender_combined.ssn_classifier`, then append:
   ```
   #standardSQL
   SELECT a.*, b.gender ssn_gender, b.probability ssn_probability, b.count ssn_count
   FROM `my-project-1491577527670.gender_combined.gender_classifiercom1` a
   LEFT JOIN `my-project-1491577527670.gender_combined.ssn_classifier` b
   ON a.first_name = b.name and (a.country_code is null or a.country_code = 'us')
   ```
   Here the table `my-project-1491577527670:gender_combined.gender_classiercom1` is from step 1, and the result is saved as `my-project-1491577527670.gender_combined.gender_classifiercom2`

3. Append wipo data:
   ```
   #standardSQL
   SELECT a.*, b.gender wipo_gender, b.probability wipo_probability, b.count wipo_count
   FROM `my-project-1491577527670.gender_combined.gender_classifiercom2` a
   LEFT JOIN `natural-venture-179401.Leaderboard.wipo_classifier` b
   ON a.first_name = b.f_name and (a.country_code is null or a.country_code = 'us')
   ```
   Save the result as `my-project-1491577527670.gender_combined.gender_classifiercom3`

4. Append genderize.io data:
   ```
   #standardSQL
   SELECT a.*, b.country_code gi_country_code, b.gender gi_gender, b.probability gi_probability, b.count gi_count
   FROM `my-project-1491577527670.gender_combined.gender_classifiercom3` a
   LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_2` b
   ON a.first_name = b.name_first and ((a.country_code is null and b.country_code is null) or a.country_code = b.country_code)
   ```
   Save the result as `my-project-1491577527670.gender_combined.gender_classifiercom4`

5. Append GenderAPI data:
   ```
   #standardSQL
   SELECT a.*, b.country ga_country_code, b.gender ga_gender, b.probability ga_probability, b.count ga_count
   FROM `my-project-1491577527670.gender_combined.gender_classifiercom4` a
   LEFT JOIN `natural-venture-179401.Leaderboard.genderapi_classifier` b
   ON a.first_name = b.first_name and ((a.country_code is null and b.country is null) or a.country_code = b.country)
   ```
   Save the result as `my-project-1491577527670.gender_combined.gender_classifiercom5`

6. Combine data from step 5:
   Apply ssn -> wipo -> genderize.io -> GenderAPI
   Rules: if the previous one has one match, use it and stop, except when genderize.io has less than 10 samples, then use GenderAPI.
   ```
   #standardSQL
   SELECT first_name,
      IF(ssn_gender IS NOT NULL, ssn_gender, IF(wipo_gender IS NOT NULL, wipo_gender, IF(gi_gender IS NOT NULL AND NOT (gi_count < 10 AND ga_count IS NOT NULL), gi_gender, IF(ga_gender IS NOT NULL, ga_gender, null)))) com_gender,
      IF(ssn_probability IS NOT NULL, ssn_probability, IF(wipo_probability IS NOT NULL, wipo_probability, IF(gi_probability IS NOT NULL AND NOT (gi_count < 10 AND ga_count IS NOT NULL), gi_probability, IF(ga_probability IS NOT NULL, ga_probability, null)))) com_probability,   
      IF(ssn_count IS NOT NULL, ssn_count, IF(wipo_count IS NOT NULL, wipo_count, IF(gi_count IS NOT NULL AND NOT (gi_count < 10 AND ga_count IS NOT NULL), gi_count, IF(ga_count IS NOT NULL, ga_count, null)))) com_count,     
      country_code,
      ssn_gender, ssn_probability, ssn_count,
      wipo_gender, wipo_probability, wipo_count,
      gi_country_code, gi_gender, gi_probability, gi_count,
      ga_country_code, ga_gender, ga_probability, ga_count
   FROM `my-project-1491577527670.gender_combined.gender_classifiercom5` a
   ```
   Save the result as `my-project-1491577527670.gender_combined.gender_classifiercom`, this is the final combine result.
