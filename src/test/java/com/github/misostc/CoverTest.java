package com.github.misostc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Base64;

public class CoverTest {
    @Test
    public void shouldGenerateCover() {
        byte[] imageJPG = new PlaylistCoverGenerator().getImageJPG(0L);
        Assert.assertNotNull(imageJPG);
        Assert.assertTrue(Base64.getEncoder().encodeToString(imageJPG).getBytes().length <= 256000 );
    }
}
