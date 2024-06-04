import pandas as pd
import numpy as np
from tabulate import tabulate

# Define the possible values for each column
names = ['Nico', 'Eric', 'Tibo', 'Dave', 'Elena', 'Camille']
lectures = ['History', 'Math', 'Chemistry', 'Physics', 'Sports', 'Geography', 'Biology', 'Philosophy']
grades = ['A', 'B', 'C', 'D', 'E', 'F']
semesters = ['Q1', 'Q2', 'Q3']

# Specify the number of random entries to generate
num_entries = 50

# Generate random data
data = {
    'Name': np.random.choice(names, num_entries),
    'Lecture': np.random.choice(lectures, num_entries),
    'Grade': np.random.choice(grades, num_entries),
    'Semester': np.random.choice(semesters, num_entries),
}

# Create a DataFrame
df = pd.DataFrame(data)

# Display the DataFrame
# print(df)

# Display the DataFrame as a markdown table
print(tabulate(df, headers='keys', tablefmt='github', showindex=False))

# Save the DataFrame to a compressed CSV file
compressed_file_path = 'random_grades.csv.gz'
df.to_csv(compressed_file_path, index=False, compression='gzip')