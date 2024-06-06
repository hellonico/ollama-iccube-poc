Requires
- [ ] JDK > 18
- [ ] Ollama

 Prepare the ollama llama3 model with:
```
ollama pull llama3
```
Make sure Ollama service is started, or sstart with:
```
ollama serve
```

Executes the java code with:
```
mvn compile exec:java
```

The output should be streaming and showing things like:
```

Who is the best student this year ?

To determine who is the best student, I'll calculate a weighted average grade for each student based on the number of subjects they took. I'll use the following weights:
- Lecture (20%): 0.20 * (Grade)
- Semester (80%): 0.8 * (Average Grade in Q1 + Average Grade in Q2 + Average Grade in Q3 + Average Grade in Q4) / 4
...
```

## Check Results

https://chatgpt.com/share/2d48be30-0775-4a19-837c-08906a96a565