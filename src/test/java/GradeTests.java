import org.hellonico.aiccube.LlamaCaller;
import org.junit.jupiter.api.Test;

public class GradeTests {

    static LlamaCaller llama = new LlamaCaller("scenario/grades/");

    @Test
    public void bestInQ4() {
        llama.test("best_student_in_q4");
    }

    @Test
    public void lectureWithBestGrades() {
        llama.test("lecture_with_best_average");
    }

}
