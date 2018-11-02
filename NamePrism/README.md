For running nameprism:
1. Configure the input, output file paths in MultiThreadRestHandler.java
2. Run the file 

This process tries to fetch the data from the nameprism website using the authtoken (part of the url itself). 
And saves the exact json response in the output.csv file that you configure. 

From the json response , parse the json and append to the original dataset. 
You can use the file ExcelMapper.java to do the same. 
