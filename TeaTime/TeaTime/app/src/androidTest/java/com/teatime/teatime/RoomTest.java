package com.teatime.teatime;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.teatime.teatime.object.AppDatabase;
import com.teatime.teatime.object.ApplicationHelper;
import com.teatime.teatime.object.GoodWithType;
import com.teatime.teatime.object.HealthPropertyType;
import com.teatime.teatime.object.Tea;
import com.teatime.teatime.object.TeaDao;
import com.teatime.teatime.object.TeaType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@RunWith(AndroidJUnit4.class)
public class RoomTest {
    private TeaDao teaDao;
    private AppDatabase appDB;

    @Before
    public void createDB() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        appDB = ApplicationHelper.getDB(ctx,true);
        teaDao = appDB.teaDao();
    }

    @After
    public void closeDB() throws IOException {
        ApplicationHelper.CloseDB();
    }
    //Generate Tea for test
    Tea getTestTea() {
        return new Tea("Test Tea", "Tea de test",
                "A perfect test Tea é à ","Un the parfait é à",
                false, EnumSet.of(TeaType.WHITE),
                EnumSet.of(HealthPropertyType.NONE), EnumSet.of(GoodWithType.NONE),60,null);
    }

    //Test Insert
    @Test
    public void testInsert() throws Exception {
        Tea t = getTestTea();

        long id = teaDao.insert(t);
        t.setId(id);

        Assert.assertTrue("Error on Insert", t.getId() == 1);

        Tea testTea = teaDao.getById(t.getId());

        Assert.assertEquals("Tea wasn't inserted properly", t, testTea);
    }

    @Test
    public void insertAll() throws Exception {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();
        Tea t3 = getTestTea();

        t1.setTeaName_EN("A");
        t1.setTeaName_FR("Z");
        t2.setTeaName_EN("B");
        t2.setTeaName_FR("Y");
        t3.setTeaName_EN("C");
        t3.setTeaName_FR("X");

        long[] ids = teaDao.insertAll(t2,t3,t1);

        Assert.assertTrue("Error on insert (wrong number of ids back)", ids.length == 3);

        t2.setId(ids[0]);
        t3.setId(ids[1]);
        t1.setId(ids[2]);

        Assert.assertTrue("Error on insert (Tea not saved properly)",
                t2.getId() == 1 && t3.getId() == 2 && t1.getId() == 3);

        Tea test_1 = teaDao.getById(t1.getId());
        Tea test_2 = teaDao.getById(t2.getId());
        Tea test_3 = teaDao.getById(t3.getId());

        Assert.assertEquals("Tea wasn't inserted properly (Retrieve tea 1)", t1, test_1);
        Assert.assertEquals("Tea wasn't inserted properly (Retrieve tea 2)", t2, test_2);
        Assert.assertEquals("Tea wasn't inserted properly (Retrieve tea 3)", t3, test_3);
    }

    //Test Queries
    //All
    @Test
    public void getAll() throws Exception {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();
        Tea t3 = getTestTea();
        Tea t4 = getTestTea();

        t1.setTeaName_EN("A");
        t1.setTeaName_FR("Z");
        t2.setTeaName_EN("B");
        t2.setFavourite(true);
        t2.setTeaName_FR("Y");
        t3.setTeaName_EN("C");
        t3.setTeaName_FR("X");
        t4.setTeaName_EN("M");
        t4.setTeaName_FR("N");
        t4.setFavourite(true);

        long[] ids = teaDao.insertAll(t2,t3,t4,t1);

        Assert.assertTrue("Error on insert (wrong number of ids back)", ids.length == 4);

        t2.setId(ids[0]);
        t3.setId(ids[1]);
        t4.setId(ids[2]);
        t1.setId(ids[3]);

        //EN  Expected t2,t4,t1,t3
        List<Tea> original_teas_EN = new ArrayList<Tea>();
        original_teas_EN.add(0, t2);
        original_teas_EN.add(1,t4);
        original_teas_EN.add(2,t1);
        original_teas_EN.add(3,t3);
        List<Tea> test_teas_EN = teaDao.getAll_EN();
        Assert.assertTrue("Error on insert (wrong number of ids back) EN", test_teas_EN.size() == 4);
        Assert.assertArrayEquals("Wrong Ordering EN",original_teas_EN.toArray(),test_teas_EN.toArray());

        //FR  Expected t4,t2,t3,t1
        List<Tea> original_teas_FR = new ArrayList<Tea>();
        original_teas_FR.add(0,t4);
        original_teas_FR.add(1,t2);
        original_teas_FR.add(2,t3);
        original_teas_FR.add(3,t1);
        List<Tea> test_teas_FR = teaDao.getAll_FR();
        Assert.assertTrue("Error on insert (wrong number of ids back) FR", test_teas_FR.size() == 4);
        Assert.assertArrayEquals("Wrong Ordering FR",original_teas_FR.toArray(),test_teas_FR.toArray());
    }

    //Search Filter
    @Test
    public void searchFilterName() throws Exception {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();
        Tea t3 = getTestTea();
        Tea t4 = getTestTea();

        t1.setTeaName_EN("ABCD");
        t1.setTeaName_FR("ZYXW");
        t2.setTeaName_EN("BCDE");
        t2.setFavourite(true);
        t2.setTeaName_FR("YXWV");
        t3.setTeaName_EN("CDEF");
        t3.setTeaName_FR("XWVU");
        t4.setTeaName_EN("JKLM");
        t4.setTeaName_FR("MNOP");
        t4.setFavourite(true);

        long[] ids = teaDao.insertAll(t2,t3,t4,t1);

        Assert.assertTrue("Error on insert (wrong number of ids back)", ids.length == 4);

        t2.setId(ids[0]);
        t3.setId(ids[1]);
        t4.setId(ids[2]);
        t1.setId(ids[3]);

        List<Tea> original_tea = new ArrayList<>();
        List<Tea> teas;

        original_tea.add(0,t2);
        original_tea.add(1,t4);
        original_tea.add(2,t1);
        original_tea.add(3,t3);

        //Name
        //EN
        //0 letter check Expected all 4 : t2,t4,t1,t3
        teas = teaDao.getAllByFilter_EN(null,TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 0 letter check EN",original_tea.toArray(), teas.toArray());
        //1 letter check Expected 3
        original_tea.remove(t4);
        teas = teaDao.getAllByFilter_EN("C",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 1 letter check EN",original_tea.toArray(), teas.toArray());
        //2 letters check Expected 2
        original_tea.remove(t3);
        teas = teaDao.getAllByFilter_EN("BC",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 2 letters check EN",original_tea.toArray(), teas.toArray());
        //3 letters checks Expected 2
        teas = teaDao.getAllByFilter_EN("BCD",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 3 letters check EN",original_tea.toArray(), teas.toArray());
        //4 letters checks Expected 1
        original_tea.remove(t2);
        teas = teaDao.getAllByFilter_EN("ABCD",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 4 letters check EN",original_tea.toArray(), teas.toArray());
        //Not Exist
        original_tea.clear();
        teas = teaDao.getAllByFilter_EN("9876654",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on not Exist name check EN",original_tea.toArray(), teas.toArray());

        //FR
        original_tea.add(0,t4);
        original_tea.add(1,t2);
        original_tea.add(2,t3);
        original_tea.add(3,t1);
        //0 letter check Expected all 4 : t4,t2,t3,t1
        teas = teaDao.getAllByFilter_FR("",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 0 letter check FR",original_tea.toArray(), teas.toArray());
        //1 letter check Expected 3
        original_tea.remove(t4);
        teas = teaDao.getAllByFilter_FR("X",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 1 letter check FR",original_tea.toArray(), teas.toArray());
        //2 letters check Expected 2
        original_tea.remove(t3);
        teas = teaDao.getAllByFilter_FR("YX",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 2 letters check FR",original_tea.toArray(), teas.toArray());
        //3 letters checks Expected 2
        teas = teaDao.getAllByFilter_FR("YXW",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 3 letters check FR",original_tea.toArray(), teas.toArray());
        //4 letters checks Expected 1
        original_tea.remove(t2);
        teas = teaDao.getAllByFilter_FR("ZYXW",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on 4 letters check FR",original_tea.toArray(), teas.toArray());
        //Not Exist
        original_tea.clear();
        teas = teaDao.getAllByFilter_FR("9876654",TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Failed on not Exist name check FR",original_tea.toArray(), teas.toArray());
    }

    @Test
    public void searchFilterType() throws Exception {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();
        Tea t3 = getTestTea();
        Tea t4 = getTestTea();

        t1.setTeaName_EN("A");
        t1.setTeaName_FR("Z");
        t2.setTeaName_EN("B");
        t2.setFavourite(true);
        t2.setTeaName_FR("Y");
        t3.setTeaName_EN("C");
        t3.setTeaName_FR("X");
        t4.setTeaName_EN("M");
        t4.setTeaName_FR("N");
        t4.setFavourite(true);

        t1.setTeaType(EnumSet.of(TeaType.BLACK));
        t2.setTeaType(EnumSet.of(TeaType.BLACK));
        t3.setTeaType(EnumSet.of(TeaType.FERMENTED));
        t4.setTeaType(EnumSet.of(TeaType.GREEN));

        long[] ids = teaDao.insertAll(t2,t3,t4,t1);

        Assert.assertTrue("Error on insert (wrong number of ids back)", ids.length == 4);

        t2.setId(ids[0]);
        t3.setId(ids[1]);
        t4.setId(ids[2]);
        t1.setId(ids[3]);

        List<Tea> original_tea = new ArrayList<Tea>();
        List<Tea> teas;
        //1 Type
        original_tea.add(0,t2);
        original_tea.add(1,t1);
        teas = teaDao.getAllByFilter_FR(null,TeaType.BLACK.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Error on retrive all BLACK Teas", original_tea.toArray(),teas.toArray());
        //2 Types
        original_tea.clear();
        original_tea.add(0,t4);
        original_tea.add(1,t3);
        teas = teaDao.getAllByFilter_FR(null,TeaType.getStatusValue(EnumSet.of(TeaType.FERMENTED,TeaType.GREEN)),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Error on retrieve all FERMENTED and GREEN Teas", original_tea.toArray(), teas.toArray());
        //Not Exist Type
        original_tea.clear();
        teas = teaDao.getAllByFilter_FR(null,TeaType.OOLONG.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Errror retrieve a TeaType that isn't in the DB", original_tea.toArray(), teas.toArray());
    }

    @Test
    public void searchFilterGoodWith() throws Exception {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();
        Tea t3 = getTestTea();
        Tea t4 = getTestTea();

        t1.setTeaName_EN("A");
        t1.setTeaName_FR("Z");
        t2.setTeaName_EN("B");
        t2.setFavourite(true);
        t2.setTeaName_FR("Y");
        t3.setTeaName_EN("C");
        t3.setTeaName_FR("X");
        t4.setTeaName_EN("M");
        t4.setTeaName_FR("N");
        t4.setFavourite(true);

        t1.setGoodWithFlags(EnumSet.of(GoodWithType.MILK, GoodWithType.SUGAR));
        t2.setGoodWithFlags(EnumSet.of(GoodWithType.MILK));
        t3.setGoodWithFlags(EnumSet.of(GoodWithType.OTHER));
        t4.setGoodWithFlags(EnumSet.of(GoodWithType.OTHER,GoodWithType.MILK));

        long[] ids = teaDao.insertAll(t2,t3,t4,t1);

        Assert.assertTrue("Error on insert (wrong number of ids back)", ids.length == 4);

        t2.setId(ids[0]);
        t3.setId(ids[1]);
        t4.setId(ids[2]);
        t1.setId(ids[3]);

        List<Tea> original_teas = new ArrayList<Tea>();
        List<Tea> teas;

        //GoodWith 1 Flag
        original_teas.add(0,t1);
        teas = teaDao.getAllByFilter_FR(null,TeaType.ALL.getValue(),GoodWithType.SUGAR.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Error on 1 GoodWith search", original_teas.toArray(), teas.toArray());
        //GoodWith 2 Flag
        original_teas.add(0,t4);
        original_teas.add(1, t2);
        teas = teaDao.getAllByFilter_FR(null,TeaType.ALL.getValue(),GoodWithType.getStatusValue(EnumSet.of(GoodWithType.MILK,GoodWithType.SUGAR)),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Error on 2 Goodwith flags search",original_teas.toArray(),teas.toArray());
        //GoodWith Not exist flag
        original_teas.clear();
        teas = teaDao.getAllByFilter_FR(null,TeaType.ALL.getValue(),GoodWithType.NONE.getValue(),HealthPropertyType.ALL.getValue());
        Assert.assertArrayEquals("Error on Not Exist GoodWith flag search",original_teas.toArray(),teas.toArray());
    }

    @Test
    public void searchFilterHealthProperty() throws Exception {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();
        Tea t3 = getTestTea();
        Tea t4 = getTestTea();

        t1.setTeaName_EN("A");
        t1.setTeaName_FR("Z");
        t2.setTeaName_EN("B");
        t2.setFavourite(true);
        t2.setTeaName_FR("Y");
        t3.setTeaName_EN("C");
        t3.setTeaName_FR("X");
        t4.setTeaName_EN("M");
        t4.setTeaName_FR("N");
        t4.setFavourite(true);

        t1.setHealthPropertyFlags(EnumSet.of(HealthPropertyType.ANTI_INFLAMMATORY));
        t2.setHealthPropertyFlags(EnumSet.of(HealthPropertyType.ANTI_INFLAMMATORY,HealthPropertyType.CAFFEINE));
        t3.setHealthPropertyFlags(EnumSet.of(HealthPropertyType.CAFFEINE));
        t4.setHealthPropertyFlags(EnumSet.of(HealthPropertyType.CAFFEINE));

        long[] ids = teaDao.insertAll(t2,t3,t4,t1);

        Assert.assertTrue("Error on insert (wrong number of ids back)", ids.length == 4);

        t2.setId(ids[0]);
        t3.setId(ids[1]);
        t4.setId(ids[2]);
        t1.setId(ids[3]);

        List<Tea> original_teas = new ArrayList<Tea>();
        List<Tea> teas;

        //HealthProperty 1 flag
        original_teas.add(0,t2);
        original_teas.add(1,t1);
        teas = teaDao.getAllByFilter_FR(null,TeaType.ALL.getValue(),GoodWithType.ALL.getValue(),HealthPropertyType.ANTI_INFLAMMATORY.getValue());
        Assert.assertArrayEquals("",original_teas.toArray(),teas.toArray());
        //HealthProperty 2 flags
        original_teas.add(0,t4);
        original_teas.add(2,t3);
        teas = teaDao.getAllByFilter_FR(null,TeaType.ALL.getValue(), GoodWithType.ALL.getValue(),HealthPropertyType.getStatusValue(EnumSet.of(HealthPropertyType.ANTI_INFLAMMATORY,HealthPropertyType.CAFFEINE)));
        //HealthProperty Not Exist
        original_teas.clear();
        teas = teaDao.getAllByFilter_FR(null,TeaType.ALL.getValue(), GoodWithType.ALL.getValue(), HealthPropertyType.NONE.getValue());
        Assert.assertArrayEquals("", original_teas.toArray(), teas.toArray());
    }

    @Test
    public void setFavourite() {
        Tea t1 = getTestTea();
        Tea t2 = getTestTea();

        t1.setTeaName_EN("A");
        t1.setTeaName_FR("Z");
        t1.setFavourite(false);
        t2.setTeaName_EN("B");
        t2.setTeaName_FR("Y");
        t2.setFavourite(true);

        long[] ids = teaDao.insertAll(t1,t2);

        t1.setId(ids[0]);
        t2.setId(ids[1]);

        //Set both to false and check
        t2.setFavourite(false);
        teaDao.setFavourite(t2.getId(),false);
        Tea test_t1 = teaDao.getById(t1.getId());
        Tea test_t2 = teaDao.getById(t2.getId());
        Assert.assertEquals("Error : Set All to false",t1,test_t1);
        Assert.assertEquals("Error : Didn't change to false",t2,test_t2);

        //Set t1 to true and check
        t1.setFavourite(true);
        teaDao.setFavourite(t1.getId(),true);
        test_t1 = teaDao.getById(t1.getId());
        test_t2 = teaDao.getById(t2.getId());
        Assert.assertEquals("Error : Didn't change to true",t1,test_t1);
        Assert.assertEquals("Error : Set All to true",t2,test_t2);
    }
}

