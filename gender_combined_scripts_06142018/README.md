## SQL scripts to combine wipo / jenson gender classifier

* wipo source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.wipo_classifier
* jenson source: https://bigquery.cloud.google.com/table/natural-venture-179401:Leaderboard.jenson_gendercoded


### SQL scripts to convert wipo_classifier to the same format with jenson_gendercoded
```
SELECT first_name, country_code, CASE WHEN gender = 'male' THEN 1 - prob_female ELSE prob_female END prob_female, count frequency
FROM [natural-venture-179401:Leaderboard.wipo_classifier]
```
Result: save as wipo_gendercoded

### SQL Scripts to Apply Combined Gender Classifier to each dataset
Apply with wipo first; then jenson
1. To US_AI_cleaned
  ```
  #standardSQL
  SELECT c.application_number, c.filing_date, c.uspc_class, c.cpc4, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.inventor_rank, c.inventor_region_code, c.inventor_country_code,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_country_code, null) com_country_code,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_prob_female, d.prob_female) com_prob_female,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_frequency, d.frequency) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
    FROM `natural-venture-179401.Leaderboard.US_AI_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.wipo_gendercoded` b
    ON a.inventor_name_first = b.first_name AND lower(a.inventor_country_code) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.jenson_gendercoded` d
  ON c.inventor_name_first = d.firstname
  ```
  Result: save as US_AI_cleaned_gender_com

2. To non_US_AI_cleaned
  ```
  #standardSQL
  SELECT c.application_number, c.filing_date, c.uspc_class, c.cpc4, c.inventor_name_first, c.inventor_name_middle, c.inventor_name_last, c.inventor_rank, c.inventor_region_code, c.inventor_country_code,
  IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_country_code, null) com_country_code,
  IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_prob_female, d.prob_female) com_prob_female,
  IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_frequency, d.frequency) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
    FROM `natural-venture-179401.Leaderboard.non_US_AI_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.wipo_gendercoded` b
    ON a.inventor_name_first = b.first_name AND lower(a.inventor_country_code) = b.country_code) c
  LEFT JOIN `natural-venture-179401.Leaderboard.jenson_gendercoded` d
  ON c.inventor_name_first = d.firstname
  ```
Result: save as non_US_AI_cleaned_gender_com

3. To inventor_first_last_name_country_cpc
  ```
  #standardSQL
  SELECT c.patent_id, c.year_of_filing, c.year_of_patent, c.name_first, c.name_last, c.country, c.cpc4, c.Techn_Sector, c.Techn_Field_Name,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_country_code, null) com_country_code,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_prob_female, d.prob_female) com_prob_female,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_frequency, d.frequency) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
    FROM `natural-venture-179401.Leaderboard.inventor_first_last_name_country_cpc` a
    LEFT JOIN `natural-venture-179401.Leaderboard.wipo_gendercoded` b
    ON a.name_first = b.first_name AND b.country_code IS NULL) c
  LEFT JOIN `natural-venture-179401.Leaderboard.jenson_gendercoded` d
  ON c.name_first = d.firstname
  ```
  Result: save as inventor_first_last_name_country_cpc_gender_com

4. To aaai_cleaned
  ```
  #standardSQL
  SELECT c.full_name, c.f_name, c.affiliation, c.paper_title, c.paper_url	year,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_country_code, null) com_country_code,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_prob_female, d.prob_female) com_prob_female,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_frequency, d.frequency) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
    FROM `natural-venture-179401.Leaderboard.aaai_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.wipo_gendercoded` b
    ON a.f_name = b.first_name AND b.country_code IS NULL) c
  LEFT JOIN `natural-venture-179401.Leaderboard.jenson_gendercoded` d
  ON c.f_name = d.firstname
  ```
  Result: save as aaai_cleaned_gender_com

5. To nips_cleaned
  ```
  #standardSQL
  SELECT c.author_name_full, c.author_name_first, c.author_name_middle, c.author_name_last, c.author_url, c.paper_title, c.paper_url, c.year_of_conference,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_country_code, null) com_country_code,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_prob_female, d.prob_female) com_prob_female,
    IF(c.com_prob_female IS NULL OR (c.com_prob_female > 0.05 AND c.com_prob_female < 0.95) AND d.prob_female IS NOT NULL, c.com_frequency, d.frequency) com_count
  FROM (SELECT a.*, b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
    FROM `natural-venture-179401.Leaderboard.nips_cleaned` a
    LEFT JOIN `natural-venture-179401.Leaderboard.wipo_gendercoded` b
    ON a.author_name_first = b.first_name AND b.country_code IS NULL) c
  LEFT JOIN `natural-venture-179401.Leaderboard.jenson_gendercoded` d
  ON c.author_name_first = d.firstname
  ```
  Result: save as nips_cleaned_gender_com
