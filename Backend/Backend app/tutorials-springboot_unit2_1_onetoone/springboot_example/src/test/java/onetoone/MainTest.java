package onetoone;

import onetoone.Login.LoginRepository;
import onetoone.Persons.PersonRepository;
import onetoone.Signup.SignupRepository;
import onetoone.DiningHall.DiningHallRepository;
import onetoone.TestingCenter.TestingCenterRepository;
import onetoone.TestingCenter.ExamInfoRepository;
import onetoone.Persons.Person;
import onetoone.Login.Login;
import onetoone.Signup.Signup;
import onetoone.DiningHall.DiningHall;
import onetoone.TestingCenter.TestingCenter;
import onetoone.TestingCenter.ExamInfo;
import onetoone.UserRoles.UserRoles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@SpringBootTest(classes = Main.class)
@WebMvcTest(Main.class)
public class MainTest {

    @MockBean
    private LoginRepository loginRepository;

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private SignupRepository signupRepository;

    @MockBean
    private DiningHallRepository diningHallRepository;

    @MockBean
    private TestingCenterRepository testingCenterRepository;

    @MockBean
    private ExamInfoRepository examInfoRepository;

    @Autowired
    private ApplicationContext context;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Captor
    private ArgumentCaptor<Login> loginCaptor;

    @Captor
    private ArgumentCaptor<Signup> signupCaptor;

    @Captor
    private ArgumentCaptor<DiningHall> diningHallCaptor;

    @Captor
    private ArgumentCaptor<TestingCenter> testingCenterCaptor;

    @Captor
    private ArgumentCaptor<ExamInfo> examInfoCaptor;

//  Need to fix -->   @Test
//    public void testInitDataMethodCreatesEntities() throws Exception {
//        // Get the CommandLineRunner bean that was created
//        CommandLineRunner runner = context.getBean(CommandLineRunner.class);
//
//        // Execute the run method with empty args
//        runner.run();
//
//        // Verify that the save methods were called on each repository with the correct data
//        verify(personRepository, atLeast(4)).save(personCaptor.capture());
//        verify(loginRepository, times(4)).save(loginCaptor.capture());
//        verify(signupRepository, atLeast(4)).save(signupCaptor.capture());
//        verify(diningHallRepository, times(2)).save(diningHallCaptor.capture());
//        verify(testingCenterRepository, times(2)).save(testingCenterCaptor.capture());
//        verify(examInfoRepository, times(4)).save(examInfoCaptor.capture());
//
//        // Test person entities
//        List<Person> capturedPersons = personCaptor.getAllValues();
//        assertTrue(capturedPersons.stream().anyMatch(p -> p.getName().equals("Michael Johnson")));
//        assertTrue(capturedPersons.stream().anyMatch(p -> p.getName().equals("Sarah Adams")));
//        assertTrue(capturedPersons.stream().anyMatch(p -> p.getName().equals("David Williams")));
//        assertTrue(capturedPersons.stream().anyMatch(p -> p.getName().equals("Sonia Patil")));
//
//        // Test login entities
//        List<Login> capturedLogins = loginCaptor.getAllValues();
//        assertTrue(capturedLogins.stream().anyMatch(l -> l.getName().equals("mjohnson")));
//        assertTrue(capturedLogins.stream().anyMatch(l -> l.getName().equals("sarah_a")));
//        assertTrue(capturedLogins.stream().anyMatch(l -> l.getName().equals("dwilliams")));
//        assertTrue(capturedLogins.stream().anyMatch(l -> l.getName().equals("SoniaP")));
//
//        // Test signup entities
//        List<Signup> capturedSignups = signupCaptor.getAllValues();
//        assertTrue(capturedSignups.stream().anyMatch(s -> s.getUsername().equals("Michael Johnson")));
//        assertTrue(capturedSignups.stream().anyMatch(s -> s.getUsername().equals("Sarah Adams")));
//
//        // Test dining hall entities
//        List<DiningHall> capturedDiningHalls = diningHallCaptor.getAllValues();
//        assertTrue(capturedDiningHalls.stream().anyMatch(d -> d.getName().equals("UDCC")));
//        assertTrue(capturedDiningHalls.stream().anyMatch(d -> d.getName().equals("Windows")));
//
//        // Test testing center entities
//        List<TestingCenter> capturedTestingCenters = testingCenterCaptor.getAllValues();
//        assertTrue(capturedTestingCenters.stream().anyMatch(t -> t.getLocation().equals("Carver 101")));
//        assertTrue(capturedTestingCenters.stream().anyMatch(t -> t.getLocation().equals("Troxel 1001")));
//
//        // Test exam info entities
//        List<ExamInfo> capturedExamInfos = examInfoCaptor.getAllValues();
//        assertTrue(capturedExamInfos.stream().anyMatch(e -> e.getExamName().equals("math")));
//        assertTrue(capturedExamInfos.stream().anyMatch(e -> e.getExamName().equals("physics")));
//        assertTrue(capturedExamInfos.stream().anyMatch(e -> e.getExamName().equals("JAVA Intro")));
//        assertTrue(capturedExamInfos.stream().anyMatch(e -> e.getExamName().equals("English Intro")));
//    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(context);
    }
}