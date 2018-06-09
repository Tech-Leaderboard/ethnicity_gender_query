## SQL scripts to combine ssn / wipo / genderize.io / GenderAPI gender classifier

* ssn source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.gender_classifier_1
* wipo source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.wipo_classifier
(These two use no country code)
* genderize.io source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.gender_classifier_2
* GenderAPI source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.genderapi_classifier
(These two use country code)


### SQL Scripts to Combine Gender Classifier
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
      (SELECT inventor_name_first first_name, STRING(NULL) country_code
      FROM[natural-venture-179401:Leaderboard.decades_cleaned]),
      (SELECT inventor_name_first first_name, STRING(NULL) country_code
      FROM[natural-venture-179401:Leaderboard.non_US_all_cleaned_gender2]),
      (SELECT inventor_name_first first_name, STRING(NULL) country_code
      FROM[natural-venture-179401:Leaderboard.non_US_AI_cleaned_gender2]),
      (SELECT inventor_name_first first_name, STRING(NULL) country_code
      FROM[natural-venture-179401:Leaderboard.US_AI_cleaned_gender2]),
      (SELECT inventor_name_first first_name, STRING(NULL) country_code
      FROM[natural-venture-179401:Leaderboard.US_all_cleaned_gender2]),
      (SELECT string_field_2 first_name, lower(string_field_1) country_code
      FROM[my-project-1491577527670:patents.inventor_2015_2018_split]),
      (SELECT string_field_2 first_name, STRING(NULL) country_code
      FROM[my-project-1491577527670:patents.inventor_2015_2018_split]),
      (SELECT f_name first_name, STRING(NULL) country_code
      FROM [natural-venture-179401:Leaderboard.aaai_cleaned]),
      (SELECT author_name_first first_name, STRING(NULL) country_code
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
   FROM `my-project-1491577527670.gender_combined.names2query` a
   LEFT JOIN `my-project-1491577527670.gender_combined.ssn_classifier` b
   ON a.first_name = b.name and (a.country_code is null or a.country_code = 'us')
   ```
   Here the table `my-project-1491577527670:gender_combined.names2query` is from step 1, and the result is saved as `my-project-1491577527670.gender_combined.gender_classifiercom2`

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
   Rules: if the previous one has one match >= 0.95, use it and stop; If none mataches >= 0.95, use the first one that has a value.
   ```
   python combine.py
   ```
   Upload the result csv to BigQuery, load csv into table: `my-project-1491577527670.gender_combined.gender_classifiercom`.
   Then copy the table to be: `natural-venture-179401.Leaderboard.gender_classifier_com`


### SQL Scripts to Apply Combined Gender Classifier to each dataset
Apply with (name, country_code) first; if not match, then apply only name
1. To decades_cleaned
  ```
  #standardSQL
  SELECT c.patent_id, c.patent_issue_date, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.city, c.state, c.country,
    IF(c.com_gender IS NOT NULL, c.com_country_code, d.country_code) com_country_code,
    IF(c.com_gender IS NOT NULL, c.com_gender, d.com_gender) com_gender,
    IF(c.com_probability IS NOT NULL, c.com_probability, d.com_probability) com_probability,
    IF(c.com_count IS NOT NULL, c.com_count, d.com_count)
  FROM (SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
    FROM `natural-venture-179401.Leaderboard.decades_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
    ON a.inventor_name_first = b.first_name AND LOWER(a.country) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` d
  ON c.inventor_name_first = d.first_name AND (d.country_code IS NULL OR d.country_code = '')
  ```
Result: save as decades_cleaned_gender_com
Note: make sure the classifier has not duplicated data, otherwise the result may have more rows than original one.

2. To non_US_AI_cleaned
  ```
  #standardSQL
  SELECT c.application_number, c.filing_date, c.uspc_class, c.cpc4, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.inventor_rank, c.inventor_region_code, c.inventor_country_code,
    IF(c.com_gender IS NOT NULL, c.com_country_code, d.country_code) com_country_code,
    IF(c.com_gender IS NOT NULL, c.com_gender, d.com_gender) com_gender,
    IF(c.com_probability IS NOT NULL, c.com_probability, d.com_probability) com_probability,
    IF(c.com_count IS NOT NULL, c.com_count, d.com_count) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
    FROM `natural-venture-179401.Leaderboard.non_US_AI_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
    ON a.inventor_name_first = b.first_name AND lower(a.inventor_country_code) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` d
  ON c.inventor_name_first = d.first_name AND d.country_code IS NULL
  ```
Result: save as non_US_AI_cleaned_gender_com

3. To non_US_all_cleaned
  ```
  #standardSQL
  SELECT c.application_number, c.filing_date, c.uspc_class, c.cpc4, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.inventor_rank, c.inventor_region_code, c.inventor_country_code,
    IF(c.com_gender IS NOT NULL, c.com_country_code, d.country_code) com_country_code,
    IF(c.com_gender IS NOT NULL, c.com_gender, d.com_gender) com_gender,
    IF(c.com_probability IS NOT NULL, c.com_probability, d.com_probability) com_probability,
    IF(c.com_count IS NOT NULL, c.com_count, d.com_count) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
    FROM `natural-venture-179401.Leaderboard.non_US_all_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
    ON a.inventor_name_first = b.first_name AND lower(a.inventor_country_code) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` d
  ON c.inventor_name_first = d.first_name AND d.country_code IS NULL
  ```
  Result: save as non_US_all_cleaned_gender_com


4. To US_AI_cleaned
  ```
  #standardSQL
  SELECT c.application_number, c.filing_date, c.uspc_class, c.cpc4, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.inventor_rank, c.inventor_region_code, c.inventor_country_code,
    IF(c.com_gender IS NOT NULL, c.com_country_code, d.country_code) com_country_code,
    IF(c.com_gender IS NOT NULL, c.com_gender, d.com_gender) com_gender,
    IF(c.com_probability IS NOT NULL, c.com_probability, d.com_probability) com_probability,
    IF(c.com_count IS NOT NULL, c.com_count, d.com_count) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
    FROM `natural-venture-179401.Leaderboard.US_AI_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
    ON a.inventor_name_first = b.first_name AND lower(a.inventor_country_code) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` d
  ON c.inventor_name_first = d.first_name AND d.country_code IS NULL
  ```
  Result: save as US_AI_cleaned_gender_com

5. To US_all_cleaned
  ```
  #standardSQL
  SELECT c.application_number, c.filing_date, c.uspc_class, c.cpc4, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.inventor_rank, c.inventor_region_code, c.inventor_country_code,
    IF(c.com_gender IS NOT NULL, c.com_country_code, d.country_code) com_country_code,
    IF(c.com_gender IS NOT NULL, c.com_gender, d.com_gender) com_gender,
    IF(c.com_probability IS NOT NULL, c.com_probability, d.com_probability) com_probability,
    IF(c.com_count IS NOT NULL, c.com_count, d.com_count) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
    FROM `natural-venture-179401.Leaderboard.US_all_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
    ON a.inventor_name_first = b.first_name AND lower(a.inventor_country_code) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` d
  ON c.inventor_name_first = d.first_name AND d.country_code IS NULL
  ```
  Result: save as US_all_cleaned_gender_com

6. To inventor_2015_2018_split
  ```
  #standardSQL
  SELECT c.application_number, c.publication_date, c.inventor_name, c.inventor_country, c.assignee_name, c.assignee_country, c.first_cpc, c.first_name,
    IF(c.com_gender IS NOT NULL, c.com_gender, d.com_gender) com_gender,
    IF(c.com_probability IS NOT NULL, c.com_probability, d.com_probability) com_probability,
    IF(c.com_count IS NOT NULL, c.com_count, d.com_count) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
    FROM `natural-venture-179401.Leaderboard.inventor_2015_2018_split` a
    LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
    ON a.first_name = b.first_name AND lower(a.inventor_country) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` d
  ON c.first_name = d.first_name AND d.country_code IS NULL
  ```
  Result: save as inventor_2015_2018_split_gender_com

7. To aaai_cleaned
  ```
  #standardSQL
  SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
  FROM `natural-venture-179401.Leaderboard.aaai_cleaned` a
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
  ON a.f_name = b.first_name AND b.country_code IS NULL
  ```
  Result: save as aaai_cleaned_gender_com

8. To nips_cleaned
  ```
  #standardSQL
  SELECT a.*, b.country_code com_country_code, b.com_gender, b.com_probability, b.com_count
  FROM `natural-venture-179401.Leaderboard.nips_cleaned` a
  LEFT JOIN `natural-venture-179401.Leaderboard.gender_classifier_com` b
  ON a.author_name_first = b.first_name AND b.country_code IS NULL
  ```
  Result: save as nips_cleaned_gender_com
