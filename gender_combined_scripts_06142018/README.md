## SQL scripts to combine wipo / jenson gender classifier

* wipo source: https://bigquery.cloub.google.com/table/natural-venture-179401:Leaderboarb.wipo_classifier
* jenson source: https://bigquery.cloub.google.com/table/natural-venture-179401:Leaderboarb.jenson_gendercoded


### SQL scripts to convert classifiers to the same format with jenson_gendercoded
```
SELECT first_name, country_code, CASE WHEN gender = 'male' THEN 1 - probability ELSE probability END prob_female, count frequency
FROM [natural-venture-179401:Leaderboarb.wipo_classifier]
```
Result: save as wipo_gendercoded

```
SELECT name_first, country_code, CASE WHEN gender = 'male' THEN 1 - probability ELSE probability END prob_female, count frequency
FROM [my-project-1491577527670:gender_work.gender_classifier_2]
```
Result: save as genderize_gendercoded

```
SELECT first_name, country_code, CASE WHEN gender = 'male' THEN 1 - probability ELSE probability END prob_female, count frequency
FROM [my-project-1491577527670:gender_work.genderapi_classifier]
```
Result: save as genderapi_gendercoded

### SQL Scripts to Apply Combined Gender Classifier to each dataset
1. Apply with wipo first
   1. To US_AI_cleaned
      ```
      #standardSQL
      SELECT a.*,
        b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
        FROM `my-project-1491577527670.gender_work.US_AI_cleaned` a
        LEFT JOIN `my-project-1491577527670.gender_work.wipo_gendercoded` b
        ON a.inventor_name_first = b.first_name
          AND (lower(a.inventor_country_code) = b.country_code OR a.inventor_country_code is null AND b.country_code is null)
      ```
    Result: save as US_AI_cleaned_wipo

    2. To non_US_AI_cleaned
       ```
       #standardSQL
       SELECT a.*,
         b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
         FROM `my-project-1491577527670.gender_work.non_US_AI_cleaned` a
         LEFT JOIN `my-project-1491577527670.gender_work.wipo_gendercoded` b
         ON a.inventor_name_first = b.first_name
           AND (lower(a.inventor_country_code) = b.country_code OR a.inventor_country_code is null AND b.country_code is null)
       ```
    Result: save as non_US_AI_cleaned_wipo

    3. To inventors_cleaned
       ```
       #standardSQL
       SELECT a.*,
         b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
         FROM `my-project-1491577527670.gender_work.inventors_cleaned` a
         LEFT JOIN `my-project-1491577527670.gender_work.wipo_gendercoded` b
         ON a.name_first = b.first_name AND (lower(a.country) = b.country_code or (a.country is null and b.country_code is null))
       ```
    Result: save as inventors_cleaned_wipo

    4. To aaai_cleaned
       ```
       #standardSQL
       SELECT a.*,
         b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
         FROM `my-project-1491577527670.gender_work.aaai_cleaned` a
         LEFT JOIN `my-project-1491577527670.gender_work.wipo_gendercoded` b
         ON a.f_name = b.first_name AND b.country_code is null
       ```
    Result: save as aaai_cleaned_wipo

    5. To nips_cleaned
       ```
       #standardSQL
       SELECT a.*,
         b.country_code com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
         FROM `my-project-1491577527670.gender_work.nips_cleaned` a
         LEFT JOIN `my-project-1491577527670.gender_work.wipo_gendercoded` b
         ON a.author_name_first = b.first_name AND b.country_code is null
       ```
    Result: save as nips_cleaned_wipo

2. Then apply with Jensen data
   1. To US_AI_cleaned_wipo
      ```
      #standardSQL
      SELECT a.application_number, a.filing_date, a.uspc_class, a.cpc4, a.inventor_name_first, a.inventor_name_middle, a.inventor_name_last, a.inventor_rank, a.inventor_region_code, a.inventor_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.US_AI_cleaned_wipo` a
      LEFT JOIN `my-project-1491577527670.gender_work.jenson_gendercoded` b
      ON a.inventor_name_first = b.firstname
      ```
   Result: save as US_AI_cleaned_wipo_jensen

   2. To non_US_AI_cleaned_wipo
      ```
      #standardSQL
      SELECT a.application_number, a.filing_date, a.uspc_class, a.cpc4, a.inventor_name_first, a.inventor_name_middle, a.inventor_name_last, a.inventor_rank, a.inventor_region_code, a.inventor_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.non_US_AI_cleaned_wipo` a
      LEFT JOIN `my-project-1491577527670.gender_work.jenson_gendercoded` b
      ON a.inventor_name_first = b.firstname
      ```
   Result: save as non_US_AI_cleaned_wipo_jensen

   3. To inventors_cleaned_wipo
      ```
      #standardSQL
      SELECT a.patent_id, a.year_of_filing, a.year_of_patent, a.name_first, a.name_last, a.country, a.cpc4, a.Techn_Sector, a.Techn_Field_Name,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.inventors_cleaned_wipo` a
      LEFT JOIN `my-project-1491577527670.gender_work.jenson_gendercoded` b
      ON a.name_first = b.firstname
      ```
   Result: save as inventors_cleaned_wipo_jensen

   4. To aaai_cleaned_wipo
      ```
      #standardSQL
      SELECT a.full_name, a.f_name, a.affiliation, a.paper_title, a.paper_url, a.year,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.aaai_cleaned_wipo` a
      LEFT JOIN `my-project-1491577527670.gender_work.jenson_gendercoded` b
      ON a.f_name = b.firstname
      ```
   Result: save as aaai_cleaned_wipo_jensen

   5. To nips_cleaned_wipo
      ```
      #standardSQL
      SELECT a.author_name_full, a.author_name_first, a.author_name_middle, a.author_name_last, a.author_url, a.paper_title, a.paper_url,   a.year_of_conference,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.nips_cleaned_wipo` a
      LEFT JOIN `my-project-1491577527670.gender_work.jenson_gendercoded` b
      ON a.author_name_first = b.firstname
      ```
    Result: save as nips_cleaned_wipo_jensen

3. Then apply with genderize.io
   1. To US_AI_cleaned_wipo_jensen
      ```
      #standardSQL
      SELECT a.application_number, a.filing_date, a.uspc_class, a.cpc4, a.inventor_name_first, a.inventor_name_middle,  a.inventor_name_last, a.inventor_rank, a.inventor_region_code, a.inventor_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.US_AI_cleaned_wipo_jensen` a
      LEFT JOIN `my-project-1491577527670.gender_work.genderize_gendercoded` b
      ON a.inventor_name_first = b.name_first and b.country_code is null
      ```
   Result: save as US_AI_cleaned_wipo_jensen_genderize

   2. To non_US_AI_cleaned_wipo_jensen
      ```
      #standardSQL
      SELECT a.application_number, a.filing_date, a.uspc_class, a.cpc4, a.inventor_name_first, a.inventor_name_middle, a.inventor_name_last, a.inventor_rank, a.inventor_region_code, a.inventor_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.non_US_AI_cleaned_wipo_jensen` a
      LEFT JOIN `my-project-1491577527670.gender_work.genderize_gendercoded` b
      ON a.inventor_name_first = b.name_first and b.country_code is null
      ```
   Result: save as non_US_AI_cleaned_wipo_jensen_genderize

   3. To inventor_cleaned
      ```
      #standardSQL
      SELECT a.patent_id, a.year_of_filing, a.year_of_patent, a.name_first, a.name_last, a.country, a.cpc4, a.Techn_Sector,   a.Techn_Field_Name,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.inventors_cleaned_wipo_jensen` a
      LEFT JOIN `my-project-1491577527670.gender_work.genderize_gendercoded` b
      ON a.name_first = b.name_first and b.country_code is null
     ```
   Result: save as inventors_cleaned_wipo_jensen_genderize

   4. To aaai_cleaned_wipo_jensen
      ```
      #standardSQL
      SELECT a.full_name, a.f_name, a.affiliation, a.paper_title, a.paper_url, a.year,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.aaai_cleaned_wipo_jensen` a
      LEFT JOIN `my-project-1491577527670.gender_work.genderize_gendercoded` b
      ON a.f_name = b.name_first and b.country_code is null
      ```
     Result: save as aaai_cleaned_wipo_jensen_genderize

   5. To nips_cleaned_wipo_jensen
      ```
      #standardSQL
      SELECT a.author_name_full, a.author_name_first, a.author_name_middle, a.author_name_last, a.author_url, a.paper_title, a.paper_url,   a.year_of_conference,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.nips_cleaned_wipo_jensen` a
      LEFT JOIN `my-project-1491577527670.gender_work.genderize_gendercoded` b
      ON a.author_name_first = b.name_first and b.country_code is null
     ```
   Result: save as nips_cleaned_wipo_jensen_genderize

4. Then apply with Gender API
   1. To US_AI_cleaned_wipo_jensen_genderize
      ```
      #standardSQL
      SELECT a.application_number, a.filing_date, a.uspc_class, a.cpc4, a.inventor_name_first, a.inventor_name_middle,  a.inventor_name_last, a.inventor_rank, a.inventor_region_code, a.inventor_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
        IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
      FROM `my-project-1491577527670.gender_work.US_AI_cleaned_wipo_jensen_genderize` a
      LEFT JOIN `my-project-1491577527670.gender_work.genderapi_gendercoded` b
      ON a.inventor_name_first = b.first_name and b.country is null
      ```
    Result: save as US_AI_cleaned_wipo_jensen_genderize_genderapi

    2. To non_US_AI_cleaned_wipo_jensen_genderize
       ```
       #standardSQL
       SELECT a.application_number, a.filing_date, a.uspc_class, a.cpc4, a.inventor_name_first, a.inventor_name_middle,  a.inventor_name_last, a.inventor_rank, a.inventor_region_code, a.inventor_country_code,
       IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
       IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
       IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
       FROM `my-project-1491577527670.gender_work.non_US_AI_cleaned_wipo_jensen_genderize` a
       LEFT JOIN `my-project-1491577527670.gender_work.genderapi_gendercoded` b
       ON a.inventor_name_first = b.first_name and b.country is null
       ```
    Result: save as non_US_AI_cleaned_wipo_jensen_genderize_genderapi

    3. To inventors_cleaned_wipo_jensen_genderize
       ```
       #standardSQL
       SELECT a.patent_id, a.year_of_filing, a.year_of_patent, a.name_first, a.name_last, a.country, a.cpc4, a.Techn_Sector,   a.Techn_Field_Name,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
       FROM `my-project-1491577527670.gender_work.inventors_cleaned_wipo_jensen_genderize` a
       LEFT JOIN `my-project-1491577527670.gender_work.genderapi_gendercoded` b
       ON a.name_first = b.first_name and b.country is null
       ```
    Result: save as inventors_cleaned_wipo_jensen_genderize_genderapi

    4. To aaai_cleaned_wipo_jensen_genderize
       ```
       #standardSQL
       SELECT a.full_name, a.f_name, a.affiliation, a.paper_title, a.paper_url, a.year,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
       FROM `my-project-1491577527670.gender_work.aaai_cleaned_wipo_jensen_genderize` a
       LEFT JOIN `my-project-1491577527670.gender_work.genderapi_gendercoded` b
       ON a.f_name = b.first_name and b.country is null
       ```
       Result: save as aaai_cleaned_wipo_jensen_genderize_genderapi

    5. To nips_cleaned_wipo_jensen_genderize
       ```
       #standardSQL
       SELECT a.author_name_full, a.author_name_first, a.author_name_middle, a.author_name_last, a.author_url, a.paper_title, a.paper_url,   a.year_of_conference,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
         IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
       FROM `my-project-1491577527670.gender_work.nips_cleaned_wipo_jensen_genderize` a
       LEFT JOIN `my-project-1491577527670.gender_work.genderapi_gendercoded` b
       ON a.author_name_first = b.first_name and b.country is null
       ```
      Result: save as nips_cleaned_wipo_jensen_genderize_genderapi


### SQL Scripts to Do the Reverse Rrder for inventors_cleaned
1. Apply with Jensen data, to inventors_cleaned_wipo
   ```
   #standardSQL
   SELECT a.*,
     null com_country_code, b.prob_female com_prob_female, b.frequency com_frequency
   FROM `my-project-1491577527670.gender_work.inventors_cleaned` a
   LEFT JOIN `my-project-1491577527670.gender_work.jenson_gendercoded` b
   ON a.name_first = b.firstname
   ```
   Result: save as inventors_cleaned_jensen

2. Apply with Genderize.io data, to inventor_cleaned
   ```
   #standardSQL
   SELECT a.patent_id, a.year_of_filing, a.year_of_patent, a.name_first, a.name_last, a.country, a.cpc4, a.Techn_Sector, a.Techn_Field_Name,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
   FROM `my-project-1491577527670.gender_work.inventors_cleaned_jensen` a
   LEFT JOIN `my-project-1491577527670.gender_work.genderize_gendercoded` b
   ON a.name_first = b.name_first and b.country_code is null
   ```
   Result: save as inventors_cleaned_jensen_genderize

3. Apply with GenderAPI data, to inventors_cleaned_jensen_genderize
   ```
   #standardSQL
   SELECT a.patent_id, a.year_of_filing, a.year_of_patent, a.name_first, a.name_last, a.country, a.cpc4, a.Techn_Sector, a.Techn_Field_Name,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
   FROM `my-project-1491577527670.gender_work.inventors_cleaned_jensen_genderize` a
   LEFT JOIN `my-project-1491577527670.gender_work.genderapi_gendercoded` b
   ON a.name_first = b.first_name and b.country is null
   ```
   Result: save as inventors_cleaned_jensen_genderize_genderapi

4. Apply with wipo data, to inventors_cleaned
   ```
   #standardSQL
   SELECT a.patent_id, a.year_of_filing, a.year_of_patent, a.name_first, a.name_last, a.country, a.cpc4, a.Techn_Sector, a.Techn_Field_Name,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_country_code, null) com_country_code,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_prob_female, b.prob_female) com_prob_female,
     IF(a.com_prob_female <= 0.1 OR a.com_prob_female >= 0.9 OR b.prob_female IS NULL, a.com_frequency, b.frequency) com_frequency
     FROM `my-project-1491577527670.gender_work.inventors_cleaned_jensen_genderize_genderapi` a
     LEFT JOIN `my-project-1491577527670.gender_work.wipo_gendercoded` b
     ON a.name_first = b.first_name AND (lower(a.country) = b.country_code or (a.country is null and b.country_code is null))
   ```
   Result: save as inventors_cleaned_jensen_genderize_genderapi_wipo
