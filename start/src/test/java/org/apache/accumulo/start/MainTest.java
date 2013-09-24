package org.apache.accumulo.start;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

public class MainTest {
  private static final Class MAIN_CLASS = String.class;  // arbitrary
  private static final String MAIN_CLASS_NAME = MAIN_CLASS.getName();

  private JarFile f;
  private ClassLoader cl;
  @Before public void setUp() {
    f = createMock(JarFile.class);
    cl = createMock(ClassLoader.class);
  }
  @Test public void testLoadClassFromJar_ExplicitMainClass() throws Exception {
    String[] args = { "jar", "the.jar", "main.class", "arg1", "arg2" };
    expect(cl.loadClass("main.class")).andReturn(MAIN_CLASS);
    replay(cl);
    assertEquals(MAIN_CLASS, Main.loadClassFromJar(args, f, cl));
  }
  @Test public void testLoadClassFromJar_ManifestMainClass() throws Exception {
    String[] args = { "jar", "the.jar", "arg1", "arg2" };
    expect(cl.loadClass("arg1")).andThrow(new ClassNotFoundException());
    expect(cl.loadClass(MAIN_CLASS_NAME)).andReturn(MAIN_CLASS);
    replay(cl);
    mockManifestMainClass(f, MAIN_CLASS.getName());
    replay(f);
    assertEquals(MAIN_CLASS, Main.loadClassFromJar(args, f, cl));
  }
  @Test(expected=ClassNotFoundException.class)
  public void testLoadClassFromJar_NoMainClass() throws Exception {
    String[] args = { "jar", "the.jar", "arg1", "arg2" };
    expect(cl.loadClass("arg1")).andThrow(new ClassNotFoundException());
    replay(cl);
    mockManifestMainClass(f, null);
    replay(f);
    Main.loadClassFromJar(args, f, cl);
  }
  @Test(expected=ClassNotFoundException.class)
  public void testLoadClassFromJar_NoMainClassNoArgs() throws Exception {
    String[] args = { "jar", "the.jar" };
    mockManifestMainClass(f, null);
    replay(f);
    Main.loadClassFromJar(args, f, cl);
  }

  @Test(expected=ClassNotFoundException.class)
  public void testLoadClassFromJar_ExplicitMainClass_Fail() throws Exception {
    String[] args = { "jar", "the.jar", "main.class", "arg1", "arg2" };
    expect(cl.loadClass("main.class")).andThrow(new ClassNotFoundException());
    replay(cl);
    mockManifestMainClass(f, null);
    replay(f);
    Main.loadClassFromJar(args, f, cl);
  }
  @Test(expected=ClassNotFoundException.class)
  public void testLoadClassFromJar_ManifestMainClass_Fail() throws Exception {
    String[] args = { "jar", "the.jar", "arg1", "arg2" };
    expect(cl.loadClass("arg1")).andThrow(new ClassNotFoundException());
    expect(cl.loadClass(MAIN_CLASS_NAME)).andThrow(new ClassNotFoundException());
    replay(cl);
    mockManifestMainClass(f, MAIN_CLASS.getName());
    replay(f);
    Main.loadClassFromJar(args, f, cl);
  }

  private void mockManifestMainClass(JarFile f, String mainClassName)
    throws Exception {
    Manifest mf = createMock(Manifest.class);
    expect(f.getManifest()).andReturn(mf);
    Attributes attrs = createMock(Attributes.class);
    expect(mf.getMainAttributes()).andReturn(attrs);
    replay(mf);
    expect(attrs.getValue(Attributes.Name.MAIN_CLASS)).andReturn(mainClassName);
    replay(attrs);
  }
}