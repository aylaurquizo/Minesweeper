How to run:
1. Install Java Development Kit (JDK)
   You can download it from Oracle JDK or use OpenJDK.
   After installation, confirm it by running the following command in terminal:
      `java -version`

2. Install Git
   Confirm the installation by running:
      `git --version`

3. Clone Repository
   Type this into terminal:
      `git clone https://github.com/aylaurquizo/Minesweeper.git`

4. Navigate to the Project Directory
   After cloning, navigate to the project folder by typing in terminal:
      `cd Minesweeper/HW9`

5. Compile the Java Files  
   Compile the .java files in the project by typing in terminal:  
   `javac -cp "libs/*" -d bin src/**/*.java`

7. Run the Main Class
   After compilation, run the main class by typing in terminal:
      `java -cp "bin:libs/*" Main`

8. Run Tests (Optional)
   If you would like to see the tests run, you can run them by executing:
      `java -cp "bin:libs/*" tester.Main ExamplesMinesweeper`
