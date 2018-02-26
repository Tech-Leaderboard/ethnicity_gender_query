### How to run this script

1. Select the distinct first names from BigQuery, with the format a name a line, save the names in the file: names2query.csv.
   Select Example:
   ```
   SELECT inventor_name_first
   FROM (SELECT * FROM[natural-venture-179401:Leaderboard.decades_cleaned_gender2]),
   (SELECT * FROM[natural-venture-179401:Leaderboard.non_US_all_cleaned_gender2]),
   (SELECT * FROM[natural-venture-179401:Leaderboard.non_US_AI_cleaned_gender2]),
   (SELECT * FROM[natural-venture-179401:Leaderboard.US_AI_cleaned_gender2]),
   (SELECT * FROM[natural-venture-179401:Leaderboard.US_all_cleaned_gender2])
   GROUP BY inventor_name_first
   ```

2. Run the script:
   ```
   python run.py
   ```

3. Repeat 2 until get all the names results

4. Upload the data into BigQuery, and execute the SQL to merge the results.
   Select example:
   ```
   SELECT a.patent_id as patent_id,
          a.patent_issue_date as patent_issue_date,
          a.inventor_name_first as inventor_name_first,
          a.inventor_name_middle as inventor_name_middle,
          a.inventor_name_last as inventor_name_last,
          a.city as city,
          a.state as state,
          a.country as country,
          if(length(a.gender) = 0 or a.gender is null, b.gender, a.gender) as gender,
          if(length(a.probability) = 0 or a.probability is null, b.probability, a.probability) as probability,
          if(length(a.count) = 0 or a.count is null, b.count, a.count) as count
   FROM [natural-venture-179401:Leaderboard.decades_cleaned_gender2] as a
   LEFT JOIN [natural-venture-179401:Leaderboard.gender2] as b
   ON a.inventor_name_first = b.inventor_name_first

   SELECT a.application_number as application_number,
          a.filing_date as filing_date,
          a.uspc_class as uspc_class,
          a.cpc4 as cpc4,
          a.inventor_name_first as inventor_name_first,
          a.inventor_name_middle as inventor_name_middle,
          a.inventor_name_last as inventor_name_last,
          a.inventor_rank as inventor_rank,
          a.inventor_region_code as inventor_region_code,
          a.inventor_country_code as inventor_country_code,
          if(length(a.gender) = 0 or a.gender is null, b.gender, a.gender) as gender,
          if(length(a.probability) = 0 or a.probability is null, b.probability, a.probability) as probability,
          if(length(a.count) = 0 or a.count is null, b.count, a.count) as count
   FROM [natural-venture-179401:Leaderboard.US_all_cleaned_gender2] as a
   LEFT JOIN [natural-venture-179401:Leaderboard.gender2] as b
   ON a.inventor_name_first = b.inventor_name_first
   ```
