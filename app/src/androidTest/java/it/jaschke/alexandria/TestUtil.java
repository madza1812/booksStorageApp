package it.jaschke.alexandria;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by An on 9/11/2015.
 */
public class TestUtil extends AndroidTestCase {
    public void testIsbn10ToIsbn13Converse() {
        assertEquals(Util.isbn10ToIsbn13Converse("0134171454"), "9780134171456");
        assertEquals(Util.isbn10ToIsbn13Converse("1507893744"), "9781507893746");
        assertEquals(Util.isbn10ToIsbn13Converse("1430266015"), "9781430266013");
        assertEquals(Util.isbn10ToIsbn13Converse("0133892387"), "9780133892383");
        assertEquals(Util.isbn10ToIsbn13Converse("0134030001"), "9780134030005");
    }
}
