package technology.dice.dicewhere.building.mmdb.ipinfo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IpInfoAnonymousResultTest {

  @Test
  public void testVpnWithValidServiceName() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("true", "false", "false", "false", "false", "ExpressVPN");

    assertTrue(
        result.vpn(), "Should be detected as VPN when vpn=true and service name is provided");
  }

  @Test
  public void testVpnWithEmptyServiceName() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("true", "false", "false", "false", "false", "");

    assertFalse(result.vpn(), "Should not be detected as VPN when service name is empty");
  }

  @Test
  public void testVpnWithBlankServiceName() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("true", "false", "false", "false", "false", "   ");

    assertFalse(result.vpn(), "Should not be detected as VPN when service name is blank");
  }

  @Test
  public void testVpnWithNullServiceName() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("true", "false", "false", "false", "false", null);

    assertFalse(result.vpn(), "Should not be detected as VPN when service name is null");
  }

  @Test
  public void testVpnFalseWithServiceName() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "ExpressVPN");

    assertFalse(
        result.vpn(), "Should not be detected as VPN when vpn=false regardless of service name");
  }

  @Test
  public void testVpnFalseWithoutServiceName() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "");

    assertFalse(result.vpn(), "Should not be detected as VPN when vpn=false and no service name");
  }

  @Test
  public void testHostingProvider() {
    IpInfoAnonymousResult resultTrue =
        new IpInfoAnonymousResult("false", "false", "false", "false", "true", "");
    IpInfoAnonymousResult resultFalse =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "");

    assertTrue(resultTrue.hostingProvider(), "Should detect hosting provider when true");
    assertFalse(resultFalse.hostingProvider(), "Should not detect hosting provider when false");
  }

  @Test
  public void testTorExitNode() {
    IpInfoAnonymousResult resultTrue =
        new IpInfoAnonymousResult("false", "true", "false", "false", "false", "");
    IpInfoAnonymousResult resultFalse =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "");

    assertTrue(resultTrue.torExitNode(), "Should detect Tor exit node when true");
    assertFalse(resultFalse.torExitNode(), "Should not detect Tor exit node when false");
  }

  @Test
  public void testResidentialProxy() {
    IpInfoAnonymousResult resultTrue =
        new IpInfoAnonymousResult("false", "false", "true", "false", "false", "");
    IpInfoAnonymousResult resultFalse =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "");

    assertTrue(resultTrue.residentialProxy(), "Should detect residential proxy when relay=true");
    assertFalse(
        resultFalse.residentialProxy(), "Should not detect residential proxy when relay=false");
  }

  @Test
  public void testPublicProxy() {
    IpInfoAnonymousResult resultTrue =
        new IpInfoAnonymousResult("false", "false", "false", "true", "false", "");
    IpInfoAnonymousResult resultFalse =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "");

    assertTrue(resultTrue.publicProxy(), "Should detect public proxy when true");
    assertFalse(resultFalse.publicProxy(), "Should not detect public proxy when false");
  }

  @Test
  public void testAllFieldsTrue() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("true", "true", "true", "true", "true", "TestService");

    assertTrue(result.vpn(), "Should be detected as VPN with service name");
    assertTrue(result.torExitNode(), "Should detect Tor exit node");
    assertTrue(result.residentialProxy(), "Should detect residential proxy");
    assertTrue(result.publicProxy(), "Should detect public proxy");
    assertTrue(result.hostingProvider(), "Should detect hosting provider");
  }

  @Test
  public void testAllFieldsFalse() {
    IpInfoAnonymousResult result =
        new IpInfoAnonymousResult("false", "false", "false", "false", "false", "");

    assertFalse(result.vpn(), "Should not be detected as VPN");
    assertFalse(result.torExitNode(), "Should not detect Tor exit node");
    assertFalse(result.residentialProxy(), "Should not detect residential proxy");
    assertFalse(result.publicProxy(), "Should not detect public proxy");
    assertFalse(result.hostingProvider(), "Should not detect hosting provider");
  }
}
