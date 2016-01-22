/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.internal.cache.wan.wancommand;

import hydra.Log;

import java.util.List;
import java.util.Properties;

import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.management.cli.Result;
import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;
import com.gemstone.gemfire.management.internal.cli.result.CommandResult;
import com.gemstone.gemfire.management.internal.cli.result.TabularResultData;

public class WanCommandGatewaySenderStopDUnitTest extends WANCommandTestBase {

  private static final long serialVersionUID = 1L;

  public WanCommandGatewaySenderStopDUnitTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
  }
  
  private CommandResult executeCommandWithIgnoredExceptions(String command) {
    final ExpectedException exln = addExpectedException("Could not connect");
    CommandResult commandResult =  executeCommand(command);
    exln.remove();
    return commandResult;
  }


  public void testStopGatewaySender_ErrorConditions() {

    Integer punePort = (Integer) vm1.invoke(WANCommandTestBase.class,
        "createFirstLocatorWithDSId", new Object[] { 1 });

    Properties props = new Properties();
    props.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    props.setProperty(DistributionConfig.DISTRIBUTED_SYSTEM_ID_NAME, "1");
    props.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[" + punePort + "]");
    createDefaultSetup(props);

    Integer nyPort = (Integer) vm2.invoke(WANCommandTestBase.class,
        "createFirstRemoteLocator", new Object[] { 2, punePort });

    vm3.invoke(WANCommandTestBase.class, "createCache",
        new Object[] { punePort });
    vm3.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });

    final DistributedMember vm1Member = (DistributedMember) vm3.invoke(
        WANCommandTestBase.class, "getMember");

    String command = CliStrings.STOP_GATEWAYSENDER + " --"
        + CliStrings.STOP_GATEWAYSENDER__ID + "=ln --"
        + CliStrings.STOP_GATEWAYSENDER__MEMBER + "=" + vm1Member.getId()
        + " --" + CliStrings.STOP_GATEWAYSENDER__GROUP + "=SenderGroup1";
    CommandResult cmdResult = executeCommandWithIgnoredExceptions(command);
    if (cmdResult != null) {
      String strCmdResult = commandResultToString(cmdResult);
      Log.getLogWriter().info(
          "testStopGatewaySender stringResult : " + strCmdResult + ">>>>");
      assertEquals(Result.Status.ERROR, cmdResult.getStatus());
      assertTrue(strCmdResult.contains(CliStrings.PROVIDE_EITHER_MEMBER_OR_GROUP_MESSAGE));
    } else {
      fail("testStopGatewaySender failed as did not get CommandResult");
    }
  }

  public void testStopGatewaySender() {

    Integer punePort = (Integer) vm1.invoke(WANCommandTestBase.class,
        "createFirstLocatorWithDSId", new Object[] { 1 });

    Properties props = new Properties();
    props.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    props.setProperty(DistributionConfig.DISTRIBUTED_SYSTEM_ID_NAME, "1");
    props.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[" + punePort + "]");
    createDefaultSetup(props);

    Integer nyPort = (Integer) vm2.invoke(WANCommandTestBase.class,
        "createFirstRemoteLocator", new Object[] { 2, punePort });

    vm3.invoke(WANCommandTestBase.class, "createCache",
        new Object[] { punePort });
    vm3.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm4.invoke(WANCommandTestBase.class, "createCache",
        new Object[] { punePort });
    vm4.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm5.invoke(WANCommandTestBase.class, "createCache",
        new Object[] { punePort });
    vm5.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });

    vm3.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm4.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm5.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm4.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm5.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });

    pause(10000);
    String command = CliStrings.STOP_GATEWAYSENDER + " --"
        + CliStrings.STOP_GATEWAYSENDER__ID + "=ln";
    CommandResult cmdResult = executeCommandWithIgnoredExceptions(command);
    if (cmdResult != null) {
      String strCmdResult = commandResultToString(cmdResult);
      Log.getLogWriter().info(
          "testStopGatewaySender stringResult : " + strCmdResult + ">>>>");
      assertEquals(Result.Status.OK, cmdResult.getStatus());

      TabularResultData resultData = (TabularResultData) cmdResult
          .getResultData();
      List<String> status = resultData.retrieveAllValues("Result");
      assertEquals(5, status.size());
      assertTrue(status.contains("Error"));
      assertTrue(status.contains("OK"));
    } else {
      fail("testStopGatewaySender failed as did not get CommandResult");
    }

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm4.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm5.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
  }

  /**
   * test to validate that the start gateway sender starts the gateway sender on
   * a member
   */
  public void testStopGatewaySender_onMember() {

    Integer punePort = (Integer) vm1.invoke(WANCommandTestBase.class,
        "createFirstLocatorWithDSId", new Object[] { 1 });

    Properties props = new Properties();
    props.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    props.setProperty(DistributionConfig.DISTRIBUTED_SYSTEM_ID_NAME, "1");
    props.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[" + punePort + "]");
    createDefaultSetup(props);

    Integer nyPort = (Integer) vm2.invoke(WANCommandTestBase.class,
        "createFirstRemoteLocator", new Object[] { 2, punePort });

    vm3.invoke(WANCommandTestBase.class, "createCache",
        new Object[] { punePort });
    vm3.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });

    vm3.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });

    final DistributedMember vm1Member = (DistributedMember) vm3.invoke(
        WANCommandTestBase.class, "getMember");
    pause(10000);
    String command = CliStrings.STOP_GATEWAYSENDER + " --"
        + CliStrings.STOP_GATEWAYSENDER__ID + "=ln --"
        + CliStrings.STOP_GATEWAYSENDER__MEMBER + "=" + vm1Member.getId();
    CommandResult cmdResult = executeCommandWithIgnoredExceptions(command);
    if (cmdResult != null) {
      String strCmdResult = commandResultToString(cmdResult);
      Log.getLogWriter().info(
          "testStopGatewaySender stringResult : " + strCmdResult + ">>>>");
      assertEquals(Result.Status.OK, cmdResult.getStatus());
      assertTrue(strCmdResult.contains("is stopped on member"));
    } else {
      fail("testStopGatewaySender failed as did not get CommandResult");
    }

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
  }

  /**
   * test to validate that the start gateway sender starts the gateway sender on
   * a group of members
   */
  public void testStopGatewaySender_Group() {

    Integer punePort = (Integer) vm1.invoke(WANCommandTestBase.class,
        "createFirstLocatorWithDSId", new Object[] { 1 });

    Properties props = new Properties();
    props.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    props.setProperty(DistributionConfig.DISTRIBUTED_SYSTEM_ID_NAME, "1");
    props.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[" + punePort + "]");
    createDefaultSetup(props);

    Integer nyPort = (Integer) vm2.invoke(WANCommandTestBase.class,
        "createFirstRemoteLocator", new Object[] { 2, punePort });

    vm3.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup1" });
    vm3.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm4.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup1" });
    vm4.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm5.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup1" });
    vm5.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });

    vm3.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm4.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm5.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm4.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm5.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });

    pause(10000);
    String command = CliStrings.STOP_GATEWAYSENDER + " --"
        + CliStrings.STOP_GATEWAYSENDER__ID + "=ln --"
        + CliStrings.STOP_GATEWAYSENDER__GROUP + "=SenderGroup1";
    CommandResult cmdResult = executeCommandWithIgnoredExceptions(command);
    if (cmdResult != null) {
      String strCmdResult = commandResultToString(cmdResult);
      Log.getLogWriter()
          .info(
              "testStopGatewaySender_Group stringResult : " + strCmdResult
                  + ">>>>");
      assertEquals(Result.Status.OK, cmdResult.getStatus());

      TabularResultData resultData = (TabularResultData) cmdResult
          .getResultData();
      List<String> status = resultData.retrieveAllValues("Result");
      assertEquals(3, status.size());
      assertFalse(status.contains("Error"));
      assertTrue(status.contains("OK"));
    } else {
      fail("testStopGatewaySender failed as did not get CommandResult");
    }

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm4.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm5.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
  }

  /**
   * Test to validate the scenario gateway sender is started when one or more
   * sender members belongs to multiple groups
   * 
   */
  public void testStopGatewaySender_MultipleGroup() {

    Integer punePort = (Integer) vm1.invoke(WANCommandTestBase.class,
        "createFirstLocatorWithDSId", new Object[] { 1 });

    Properties props = new Properties();
    props.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    props.setProperty(DistributionConfig.DISTRIBUTED_SYSTEM_ID_NAME, "1");
    props.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[" + punePort + "]");
    createDefaultSetup(props);

    Integer nyPort = (Integer) vm2.invoke(WANCommandTestBase.class,
        "createFirstRemoteLocator", new Object[] { 2, punePort });

    vm3.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup1" });
    vm3.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm4.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup1" });
    vm4.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm5.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup1, SenderGroup2" });
    vm5.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm6.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup2" });
    vm6.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });
    vm7.invoke(WANCommandTestBase.class, "createCacheWithGroups", new Object[] {
        punePort, "SenderGroup3" });
    vm7.invoke(WANCommandTestBase.class, "createSender", new Object[] { "ln",
        2, false, 100, 400, false, false, null, true });

    vm3.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm4.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm5.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm6.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });
    vm7.invoke(WANCommandTestBase.class, "startSender", new Object[] { "ln" });

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm4.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm5.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm6.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
    vm7.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });

    pause(10000);
    String command = CliStrings.STOP_GATEWAYSENDER + " --"
        + CliStrings.STOP_GATEWAYSENDER__ID + "=ln --"
        + CliStrings.STOP_GATEWAYSENDER__GROUP + "=SenderGroup1,SenderGroup2";
    CommandResult cmdResult = executeCommandWithIgnoredExceptions(command);
    if (cmdResult != null) {
      String strCmdResult = commandResultToString(cmdResult);
      Log.getLogWriter()
          .info(
              "testStopGatewaySender_Group stringResult : " + strCmdResult
                  + ">>>>");
      assertEquals(Result.Status.OK, cmdResult.getStatus());
      TabularResultData resultData = (TabularResultData) cmdResult
          .getResultData();
      List<String> status = resultData.retrieveAllValues("Result");
      assertEquals(4, status.size());
      assertFalse(status.contains("Error"));
      assertTrue(status.contains("OK"));
    } else {
      fail("testStopGatewaySender failed as did not get CommandResult");
    }

    vm3.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm4.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm5.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm6.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", false, false });
    vm7.invoke(WANCommandTestBase.class, "verifySenderState", new Object[] {
        "ln", true, false });
  }
}