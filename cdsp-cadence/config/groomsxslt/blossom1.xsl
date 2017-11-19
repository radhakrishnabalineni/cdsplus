<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="html" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:param name="oov_headers"><xsl:value-of select="/page/pageinfo/nvpair[@value='docinfo_config']/string[@name='oov_headers']"/></xsl:param>
  <xsl:param name="top_of_page_links"><xsl:value-of select="/page/pageinfo/nvpair[@value='docinfo_config']/string[@name='top_of_page_links']"/></xsl:param>
  <xsl:param name="support_image_path">/ewfrf-JAVA/Doc/images/</xsl:param>
  <xsl:param name="marketing_image_path">/ewfrf-JAVA/actCtrImg/</xsl:param>
  <xsl:param name="gallery_image_path">/ewfrf-JAVA/Doc/images/</xsl:param>

  <!-- locale information -->
  <xsl:param name="lc"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'lc']"/></xsl:param>
  <xsl:param name="cc"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'cc']"/></xsl:param>

  <xsl:param name="top_of_page_image">
    <xsl:choose>
      <xsl:when test="$lc = 'ja'"><xsl:value-of select="$gallery_image_path"/>gtabtop_ja_jp.gif</xsl:when>
      <xsl:when test="$lc  = 'ko'"><xsl:value-of select="$gallery_image_path"/>gtabtop_ko_kr.gif</xsl:when>
      <xsl:when test="$lc  = 'zh-hans'"><xsl:value-of select="$gallery_image_path"/>gtabtop_zh_cn.gif</xsl:when>
      <xsl:when test="$lc  = 'zh-hant'"><xsl:value-of select="$gallery_image_path"/>gtabtop_zh_tw.gif</xsl:when>
      <xsl:otherwise><xsl:value-of select="$gallery_image_path"/>minimtop.gif</xsl:otherwise>
    </xsl:choose>
  </xsl:param>

  <!-- translations -->
  <xsl:param name="note"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'note']"/></xsl:param>
  <xsl:param name="caution"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'caution']"/></xsl:param>
  <xsl:param name="warning"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'warning']"/></xsl:param>
  <xsl:param name="figure"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'figure']"/></xsl:param>
  <xsl:param name="issue"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'issue']"/></xsl:param>
  <xsl:param name="solution"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'solution']"/></xsl:param>
  <xsl:param name="cause"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'cause']"/></xsl:param>
  <xsl:param name="workaround"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'workaround']"/></xsl:param>
  <xsl:param name="summary"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'summary']"/></xsl:param>
  <xsl:param name="question"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'question']"/></xsl:param>
  <xsl:param name="answer"><xsl:value-of select="/page/pageinfo/nvpair[@value = 'doc_strings']/string[@name = 'answer']"/></xsl:param>

  <xsl:param name="corporate_style"><xsl:value-of select="/page/pageinfo/nvpair[@value='corporate_style']" /></xsl:param>
  <xsl:param name="printable"><xsl:value-of select="/page/pageinfo/nvpair[@value='printable']" /></xsl:param>

  <xsl:template match="c_support_doc | 
                       c_hho_marketing_doc/print.go/activity.instructions | 
                       c_hho_marketing_doc/print.go/activity.materials |
                       c_hho_marketing_doc"><xsl:call-template name="concentra_doc_build"/></xsl:template>

	<xsl:template match="c_support_doc | 
                       c_hho_marketing_doc/print.go/activity.instructions | 
                       c_hho_marketing_doc/print.go/activity.materials |
                       c_hho_marketing_doc"
                      name="concentra_doc_build"
                      mode="blank">
         <!--  <b>built w/ wildcat</b> -->
         <!-- | lc=<xsl:value-of select="$lc"/> | cc=<xsl:value-of select="$cc"/> | -->
          <span id="top"></span>
          <xsl:choose>
            <xsl:when test="$lc  = 'ko'">
              <div style="word-break: keep-all; font-size: 1.1em;">
                <xsl:apply-templates mode="concentra_doc">
                </xsl:apply-templates>
              </div>
              <br/><br/>
            </xsl:when>
            <xsl:when test="$lc  = 'ja' or
                            $lc  = 'zh-hans' or
                            $lc  = 'zh-hant'">
              <div style="word-break: break-all; font-size: 1.1em;">
                <xsl:apply-templates mode="concentra_doc"/>
              </div>
              <br/><br/>
            </xsl:when>
            <xsl:otherwise>
              <div style="font-size: 1.05em;">
                <xsl:apply-templates mode="concentra_doc"/>
              </div>
              <br/><br/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:template>

        <xsl:template match="short.title" mode="concentra_doc"/>
        <xsl:template match="title" mode="concentra_doc">
          <xsl:choose>
            <xsl:when test="parent::c_hho_marketing_doc"/>
            <xsl:when test="parent::c_support_doc">
              <xsl:if test="..//heading[@toc='yes']">
                <img border="0" src="{$gallery_image_path}gtabseps.gif" alt=""/>
                <br/>
                <div style="padding-left: 40px;">
                  <table border="0" summary="" >
                    <xsl:choose>
                      <xsl:when test="$lc  = 'ja' or
                                  $lc  = 'zh-hans' or
                                  $lc  = 'zh-hant'">
                        <xsl:for-each select="..//heading[@toc='yes']">
                          <tr>
                            <td valign="middle">
                              <img border="0" src="{$gallery_image_path}gtabblt.gif" alt=""/>
                            </td>
                            <td colspan="2" align="left">
                              <xsl:choose>
                                <xsl:when test="$printable = 'no'">
                                  <a href="#{generate-id()}">
                                    <xsl:if test="$corporate_style = 'compaq'">
                                      <xsl:attribute name="class">cpqRed</xsl:attribute>
                                    </xsl:if>
                                    <xsl:apply-templates mode="concentra_doc"/>
                                  </a>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:apply-templates mode="concentra_doc"/>
                                </xsl:otherwise>
                              </xsl:choose>
                            </td>
                          </tr>
                        </xsl:for-each>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:for-each select="..//generic/section/heading[@toc='yes']">
                          <tr>
                            <td valign="top">
                              <img border="0" src="{$gallery_image_path}gtabblt.gif" alt=""/>
                            </td>
                            <td colspan="2" align="left">
                              <xsl:choose>
                                <xsl:when test="$printable = 'no'">
                                  <a href="#{generate-id()}">
                                    <xsl:if test="$corporate_style = 'compaq'">
                                      <xsl:attribute name="class">cpqRed</xsl:attribute>
                                    </xsl:if>
                                    <xsl:apply-templates mode="concentra_doc"/>
                                  </a>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:apply-templates mode="concentra_doc"/>
                                </xsl:otherwise>
                              </xsl:choose>
                            </td>
                          </tr>
                          <xsl:for-each select="../section/heading[@toc='yes']">
                            <tr>
                              <td></td>
                              <td valign="middle">
                                <img border="0" src="{$gallery_image_path}gtabblt.gif" alt=""/>
                              </td>
                              <td align="left">
                                <xsl:choose>
                                  <xsl:when test="$printable = 'no'">
                                    <a href="#{generate-id()}">
                                      <xsl:if test="$corporate_style = 'compaq'">
                                        <xsl:attribute name="class">cpqRed</xsl:attribute>
                                      </xsl:if>
                                      <xsl:apply-templates mode="concentra_doc"/>
                                    </a>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:apply-templates mode="concentra_doc"/>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </td>
                            </tr>
                          </xsl:for-each>
                        </xsl:for-each>
                      </xsl:otherwise>
                    </xsl:choose>
                  </table>
                </div>
                <img border="0" src="{$gallery_image_path}gtabseps.gif" alt=""/>
	            </xsl:if>
            </xsl:when>
          </xsl:choose>
        </xsl:template>

        <!-- I know these two matches don't look like they make sense, but
             there was a weird case, where for some reason elements were being
             matched in the middle of HHO docs and did not have a mode setting.
             This was an attempt to account for that -->
        <xsl:template match="activity.description | 
                             media.type | 
                             finished.size | 
                             artist | 
                             supplier | 
                             downloadable.art | 
                             feature.thumbnail | 
                             graphic.thumbnail | 
                             source.artwork" 
                      mode="concentra_doc"/>

        <xsl:template match="activity.description | 
                             media.type | 
                             finished.size | 
                             artist | 
                             supplier | 
                             downloadable.art | 
                             feature.thumbnail | 
                             graphic.thumbnail | 
                             source.artwork" />

	<xsl:template match="section" mode="concentra_doc">
    <!-- <h1 style="display: inline; color: red;">Section 1 - <xsl:value-of select="count(ancestor::section)"/></h1> -->
    <xsl:if test="@id">
      <xsl:element name="a">
        <xsl:attribute name="class">udrline</xsl:attribute>
        <xsl:attribute name="name">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:element>
    </xsl:if>
    <div>
      <xsl:if test="count(ancestor::section) &gt; 0"><xsl:attribute name="style">margin: 0 0 0 40px;</xsl:attribute></xsl:if>
      <xsl:apply-templates mode="concentra_doc"/>
    </div>
	</xsl:template>
 
  <!-- this is still pretty ugly, but at least it isn't 3 matches any longer -->
	<xsl:template match="heading" mode="concentra_doc">
    <xsl:choose>
      <xsl:when test="count(ancestor::section) = 1">
        <xsl:choose>
          <xsl:when test="$oov_headers = 'true' and $printable = 'no'">
            <table border="0" cellpadding="0" cellspacing="0" width="100%" summary="">
              <xsl:if test="@toc='yes'">
                <xsl:attribute name="id">
                  <xsl:value-of select="generate-id()"/>
                </xsl:attribute>
              </xsl:if>	
              <tr class="theme">
                <td align="left" width="5"><img src="http://welcome.hp-ww.com/img/s.gif" width="5" height="1" alt=""/></td>
                <td align="left" valign="middle" width="90%"><h2 class="themeheader">
                  <xsl:choose>
                    <xsl:when test="$lc = 'ja' or $lc = 'zh-hans' or $lc = 'zh-hant' or $lc = 'ko'">
                      <xsl:attribute name="style">font-size: 1.2em; font-weight: bold;</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="style">font-size: 1.4em;</xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
                  <xsl:apply-templates mode="concentra_doc"/></h2></td>
                <td align="right" valign="middle" width="10%" style="padding-top: 2px; padding-bottom: 2px;"><xsl:if test="$top_of_page_links = 'true'"><a href="#top"><img src="{$top_of_page_image}" border="0" align="middle"/></a></xsl:if></td>
              </tr>
            </table>
          </xsl:when>
          <xsl:otherwise>
            <div>
              <xsl:choose>
                <xsl:when test="$lc = 'ja' or $lc = 'zh-hans' or $lc = 'zh-hant' or $lc = 'ko'">
                  <xsl:attribute name="style">font-size: 1.2em; font-weight: bold;</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="style">font-size: 1.4em; font-weight: bold;</xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:if test="@toc='yes'">
                <xsl:attribute name="id">
                  <xsl:value-of select="generate-id()"/>
                </xsl:attribute>
              </xsl:if>	
              <xsl:apply-templates mode="concentra_doc"/>
              <xsl:if test="$top_of_page_links = 'true' and $printable = 'no'">
                <a href="#top"><img src="{$top_of_page_image}" border="0"/></a>
              </xsl:if>
            </div>	
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <div>
          <xsl:choose>
             <xsl:when test="count(ancestor::section) = 2">
                <xsl:choose>
                  <xsl:when test="$lc = 'ja' or $lc = 'zh-hans' or $lc = 'zh-hant' or $lc = 'ko'">
                    <xsl:attribute name="style">margin: 1em 0; font-size: 1.2em;</xsl:attribute>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:attribute name="style">margin: 1em 0; font-size: 1.4em;</xsl:attribute>
                  </xsl:otherwise>
                </xsl:choose>
             </xsl:when>
             <xsl:otherwise>
               <xsl:attribute name="style">margin: 1em 0;</xsl:attribute>
             </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="@toc='yes'">
            <xsl:attribute name="id">
              <xsl:value-of select="generate-id()"/>
            </xsl:attribute>
          </xsl:if>
          <strong><xsl:apply-templates mode="concentra_doc"/></strong>
        </div>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>


	<xsl:template match="section/para" mode="concentra_doc">
   <xsl:if test="@id">
      <xsl:element name="a">
        <xsl:attribute name="class">udrline</xsl:attribute>
        <xsl:attribute name="name">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
	    </xsl:element>
    </xsl:if>
    <div style="margin: 1em 0;">
      <xsl:apply-templates mode="concentra_doc"/>
    </div>
	</xsl:template>

<!-- notes, cautions, warnings -->
	
	<xsl:template match="note" mode="concentra_doc">
	  <xsl:choose>
	    <xsl:when test="$lc  = 'ko' or
	                    $lc  = 'zh-hans' or
	                    $lc  = 'zh-hant'">
        <div style="margin: 0 0 10px 40px;">
          <table border="0" cellpadding="1" cellspacing="0" summary="" width="100%" style="background-color: #ffcc99; padding: 2px;">
            <tr valign="top">
              <td width="10%" nowrap="nowrap">
                <strong style="color :#ff0000"><xsl:value-of select="$note"/></strong>
              </td>
              <td width="90%">
                <xsl:text> </xsl:text>
                <xsl:apply-templates mode="concentra_doc"/>
              </td>
            </tr>
          </table>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <!-- this is problably not going to be valid if the enclosing template is wrapped in p tags -->
        <div style="margin: 0 0 10px 40px;">
          <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tr valign="top">
              <td width="40px"> </td>
              <td width="10%" nowrap="nowrap">
                <span style="color: #0000a0;">
                  <strong><xsl:value-of select="$note"/></strong>
                  <xsl:text> </xsl:text>
                </span>
              </td>
              <td width="90%" style="color: #0000a0">
                <xsl:apply-templates mode="concentra_doc"/>
              </td>
            </tr>
          </table>
        </div>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>
	 
	<xsl:template match="caution" mode="concentra_doc">
	  <xsl:choose>
	    <xsl:when test="$lc  = 'ko' or
	                    $lc  = 'ja' or
	                    $lc  = 'zh-hans' or
	                    $lc  = 'zh-hant'">
        <div style="margin: 0 0 10px 40px;">
          <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%" style="background-color: #ffcc99; padding: 2px;">
            <tr valign="top">
              <td width="10%" nowrap="nowrap">
                <strong style="color :#ff0000"><xsl:value-of select="$caution"/></strong>
              </td>
              <td width="90%">
                <xsl:text> </xsl:text>
                <xsl:apply-templates mode="concentra_doc"/>
              </td>
            </tr>
          </table>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div style="margin: 0 0 10px 40px;">
          <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tr valign="top">
              <td width="10%" nowrap="nowrap">
                <strong style="color: #990000"><xsl:value-of select="$caution"/></strong>
              </td>
              <td width="90%">
                <xsl:text> </xsl:text>
                <xsl:apply-templates mode="concentra_doc"/>
              </td>
            </tr>
          </table>
        </div>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>
	
	<xsl:template match="warning" mode="concentra_doc">
	  <xsl:choose>
	    <xsl:when test="$lc  = 'ko' or
	                    $lc  = 'ja' or
	                    $lc  = 'zh-hans' or
	                    $lc  = 'zh-hant'">
              <div style="margin:0 0 10px 40px;">
          <table border="0" cellpadding="1" cellspacing="0" summary="" width="100%"  style="background-color: #ffcc99; padding: 2px;">
            <tr valign="top">
              <td width="10%" nowrap="nowrap">
                <strong style="color: #ff0000"><xsl:value-of select="$warning"/></strong>
              </td>
              <td width="90%">
                <xsl:text> </xsl:text>
                <xsl:apply-templates mode="concentra_doc"/>
              </td>
            </tr>
          </table>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div style="margin: 0 0 10px 40px;">
          <table border="0" cellpadding="1" cellspacing="0" summary="" width="100%" style="color: #FF0000;">
            <tr valign="top">
              <td width="10%" nowrap="nowrap">
                <strong><xsl:value-of select="$warning"/></strong>
              </td>
              <td width="90%">
                <xsl:text> </xsl:text>
                <xsl:apply-templates mode="concentra_doc"/>
              </td>
            </tr>
          </table>
        </div>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>

  <!-- link related -->
	
	<xsl:template match="url" mode="concentra_doc">
	  <xsl:text>&#xA;</xsl:text>
      <xsl:choose>
        <xsl:when test="$printable = 'no'">
		      <xsl:element name="a">
            <xsl:choose>
              <xsl:when test="$corporate_style = 'compaq'">
                <xsl:attribute name="class">cpqUdrline</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="class">udrline</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="href">
              <xsl:value-of select="@address"/>
		        </xsl:attribute>
            <xsl:apply-templates mode="concentra_doc"/>
		      </xsl:element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="concentra_doc"/>
        </xsl:otherwise>
      </xsl:choose>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>
	
	<xsl:template match="xref" mode="concentra_doc">
		<!-- <xsl:text>&#x000A0;</xsl:text> -->
    <xsl:choose>
      <xsl:when test="$printable = 'no'">
		    <xsl:element name="a">
          <xsl:choose>
            <xsl:when test="$corporate_style = 'compaq'">
              <xsl:attribute name="class">cpqUdrline</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="class">udrline</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
			    <xsl:attribute name="href">#<xsl:value-of select="@idref"/></xsl:attribute>
			    <xsl:apply-templates mode="concentra_doc"/>
		    </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="concentra_doc"/>
      </xsl:otherwise>
    </xsl:choose>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>
	
	<xsl:template match="attachment" mode="concentra_doc">
    <xsl:variable name="image_path">
      <xsl:choose>
        <xsl:when test="ancestor::c_hho_marketing_doc"><xsl:value-of select="$marketing_image_path"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$support_image_path"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
		<xsl:choose>
			<xsl:when test="@type='flash'">
				<xsl:element name="object">
					<xsl:attribute name="classid">clsid:D27CDB6E-AE6D-11cf-96B8-444553540000</xsl:attribute>
					<xsl:attribute name="codebase">http://active.macromedia.com/flash2/cabs/swflash.cab#version=4,0,0,0</xsl:attribute>
					<xsl:attribute name="height">
						<xsl:value-of select="@height"/>
					</xsl:attribute>
					<xsl:attribute name="width">
						<xsl:value-of select="@width"/>
					</xsl:attribute>
					<xsl:element name="param">
						<xsl:attribute name="name">movie</xsl:attribute>
						<xsl:attribute name="value"><xsl:value-of select="$image_path"/><xsl:value-of select="@src"/></xsl:attribute>
					</xsl:element>
					<xsl:element name="param">
						<xsl:attribute name="name">menu</xsl:attribute>
						<xsl:attribute name="value">
							<xsl:value-of select="@menu"/>
						</xsl:attribute>
					</xsl:element>
					<xsl:element name="param">
						<xsl:attribute name="name">wmode</xsl:attribute>
						<xsl:attribute name="value">transparent</xsl:attribute>
					</xsl:element>
					<xsl:element name="param">
						<xsl:attribute name="name">quality</xsl:attribute>
						<xsl:attribute name="value">
							<xsl:value-of select="@quality"/>
						</xsl:attribute>
					</xsl:element>
					<xsl:element name="param">
						<xsl:attribute name="name">loop</xsl:attribute>
						<xsl:attribute name="value">
							<xsl:value-of select="@loop"/>
						</xsl:attribute>
					</xsl:element>
					<xsl:element name="param">
						<xsl:attribute name="name">bgcolor</xsl:attribute>
						<xsl:attribute name="value">#FFFFFF</xsl:attribute>
					</xsl:element>
					<xsl:element name="embed">
						<xsl:attribute name="src"><xsl:value-of select="$image_path"/><xsl:value-of select="@src"/></xsl:attribute>
						<xsl:attribute name="wmode">transparent</xsl:attribute>
						<xsl:attribute name="height">
							<xsl:value-of select="@height"/>
						</xsl:attribute>
						<xsl:attribute name="width">
							<xsl:value-of select="@width"/>
						</xsl:attribute>
						<xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
						<xsl:attribute name="quality">
							<xsl:value-of select="@quality"/>
						</xsl:attribute>
						<xsl:attribute name="type">application/x-shockwave-flash</xsl:attribute>
						<xsl:attribute name="pluginspage">http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="a">
					<xsl:attribute name="class">udrline</xsl:attribute>
					<xsl:attribute name="href"><xsl:value-of select="$image_path"/><xsl:value-of select="@src"/></xsl:attribute>
					<xsl:apply-templates mode="concentra_doc"/>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

  <xsl:template match="link" mode="concentra_doc">
    <xsl:text>&#xA;</xsl:text>
    <xsl:choose>
      <xsl:when test="$printable = 'no'">
	      <xsl:element name="a">
          <xsl:choose>
            <xsl:when test="$corporate_style = 'compaq'">
              <xsl:attribute name="class">cpqUdrline</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="class">udrline</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:attribute name="href">
            <xsl:value-of select="@address"/>
          </xsl:attribute>
          <xsl:apply-templates mode="concentra_doc"/>
	      </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="concentra_doc"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>    	
  <!-- inline styles -->
	
	<xsl:template match="para/inline.elements/ui.element | inline.elements/ui.element" mode="concentra_doc">
    <b><xsl:apply-templates mode="concentra_doc"/></b>
	</xsl:template>
	
	<xsl:template match="para/inline.elements/document.ref | inline.elements/document.ref" mode="concentra_doc">
		<i><xsl:apply-templates mode="concentra_doc"/></i>
	</xsl:template>
	
	<xsl:template match="code.example | control.panel | escape.sequence | message | keyboard | user.input | inline.elements/variable.inline" mode="concentra_doc">
    <xsl:choose>
      <xsl:when test="parent::heading">
        <xsl:text> </xsl:text>
	      <xsl:apply-templates mode="concentra_doc"/>
	      <xsl:text> </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <span style="font-family: Courier;"><xsl:apply-templates mode="concentra_doc"/></span>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>

	<xsl:template match="para/inline.elements/subscript | inline.elements/subscript" mode="concentra_doc">
		<sub><xsl:apply-templates mode="concentra_doc"/></sub>
	</xsl:template>

	<xsl:template match="para/inline.elements/superscript | inline.elements/superscript" mode="concentra_doc">
		<sup><xsl:apply-templates mode="concentra_doc"/></sup>
	</xsl:template>

	<xsl:template match="para/inline.elements/line.break" mode="concentra_doc">
		<br/>
		<xsl:apply-templates mode="concentra_doc"/>
	</xsl:template>


  <!-- unordered lists -->
	
	<xsl:template match="unordered-list" mode="concentra_doc">
    <ul>
      <xsl:for-each select="list-item">
        <li>
          <xsl:apply-templates mode="concentra_doc"/>
        </li>
      </xsl:for-each>
    </ul>
	</xsl:template>
	
	<xsl:template match="list-item/para" mode="concentra_doc">
    <xsl:if test="@id">
      <xsl:element name="a">
        <xsl:attribute name="class">udrline</xsl:attribute>
        <xsl:attribute name="name">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:element>
    </xsl:if>
    <div style="margin: 1em 0;">
      <xsl:apply-templates mode="concentra_doc"/>
    </div>
	</xsl:template>

  <!-- ordered lists -->
  <!-- modified this on the assumption that ordered-lists only contain list-items -->
	
	<xsl:template match="ordered-list" mode="concentra_doc">
    <xsl:apply-templates mode="concentra_doc" select="label"/>
    <ol>
      <xsl:choose>
        <xsl:when test="name(../../..) = 'ordered-list'">
          <xsl:attribute name="type">i</xsl:attribute>
        </xsl:when>
        <xsl:when test="name(../..) = 'ordered-list'">
          <xsl:attribute name="type">a</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">1</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:for-each select="list-item">
          <li>
            <xsl:apply-templates mode="concentra_doc"/>
	        </li>
      </xsl:for-each>
    </ol>
	</xsl:template>

  <!-- graphics related -->
	<xsl:template match="graphic/callout" mode="concentra_doc">
		<div>
                  <xsl:choose>
                    <xsl:when test="$lc = 'ja' or $lc = 'zh-hans' or $lc = 'zh-hant' or $lc = 'ko'">
                      <xsl:attribute name="style">margin: 1em 0; font-size: 0.9em;</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="style">margin: 1em 0; font-size: 0.9em;</xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
	          <strong>
	            <xsl:number level="single" count="callout" format="1 -"/>
	            <xsl:text> </xsl:text>
	            <xsl:apply-templates mode="concentra_doc"/>
	          </strong>
		</div>
	</xsl:template>

  <xsl:template match="label" mode="concentra_doc"/>
	<xsl:template match="graphic/url" mode="concentra_doc"/>

	<xsl:template match="image" name="render_image" mode="concentra_doc">
    <xsl:variable name="image_path">
      <xsl:choose>
        <xsl:when test="ancestor::c_hho_marketing_doc"><xsl:value-of select="$marketing_image_path"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$support_image_path"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="../label">
      <div style="margin: 1em 0;">
	      <strong>
	        <xsl:value-of select="$figure"/><xsl:text> </xsl:text>
	        <xsl:number level="any" count="label" format="1:"/>
	        <xsl:text> </xsl:text>
	        <xsl:value-of select="../label"/>
	      </strong>
	    </div>
    </xsl:if>
	  <xsl:choose>
	    <xsl:when test="../link">
	      <xsl:text>&#xA;</xsl:text>
	      <xsl:element name="a">
          <xsl:attribute name="name">
		        <xsl:value-of select="@id"/>
		      </xsl:attribute>
		      <xsl:attribute name="href">
		        <xsl:value-of select="../link/@address"/>
		      </xsl:attribute>
		      <img border="0" src="{$image_path}{@src}"/>
	      </xsl:element>
	      <xsl:text>&#xA;</xsl:text>
	    </xsl:when>
	    <xsl:when test="../url">
	      <xsl:text>&#xA;</xsl:text>
	      <xsl:element name="a">
	        <xsl:attribute name="name">
	          <xsl:value-of select="@id"/>
	        </xsl:attribute>
	        <xsl:attribute name="href">
	          <xsl:value-of select="../url/@address"/>
	        </xsl:attribute>
	        <img border="0" src="{$image_path}{@src}"/>
	      </xsl:element>
	      <xsl:text>&#xA;</xsl:text>
	    </xsl:when>
	    <xsl:when test="../xref">
        <xsl:text>&#xA;</xsl:text>
        <xsl:element name="a">
	        <xsl:attribute name="name">
		        <xsl:value-of select="@id"/>
	        </xsl:attribute>
		      <xsl:attribute name="href">
		        <xsl:value-of select="../xref/@xref"/>
		      </xsl:attribute>
		      <img border="0" src="{$image_path}{@src}"/>
	      </xsl:element>
	      <xsl:text>&#xA;</xsl:text>
	    </xsl:when>
	    <xsl:when test="@src">
        <img border="0" src="{$image_path}{@src}" alt=""/>
        <xsl:element name="a">
          <xsl:attribute name="class">udrline</xsl:attribute>
          <xsl:if test="@id">
            <xsl:attribute name="name">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <img border="0" src="{$image_path}{@src}" alt=""/>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>
	  
  <!-- Issue-Solution docs -->
  <!-- Problem-Resolution docs -->
  <!-- FAQ docs -->
	
	<xsl:template match="fix[@type='issue.solution']/issue |
	                     fix[@type='issue.solution']/solution |
	                     fix[@type='issue.solution']/cause |
	                     fix[@type='issue.solution']/workaround |
	                     fix[@type='issue.solution']/summary |
	                     fix[@type='problem.resolution']/issue |
			                 fix[@type='problem.resolution']/solution |
			                 fix[@type='problem.resolution']/cause |
	                     fix[@type='problem.resolution']/workaround |
	                     fix[@type='question.answer']/issue |
			                 fix[@type='question.answer']/solution |
			                 fix[@type='question.answer']/cause |
			                 fix[@type='question.answer']/workaround |
	                     fix[@type='question.answer']/summary" mode="concentra_doc">
    <div style="margin: 1em 0;">
      <strong>
        <xsl:choose>
          <xsl:when test="$lc = 'ja' or $lc = 'zh-hans' or $lc = 'zh-hant' or $lc = 'ko'">
            <xsl:attribute name="style">font-size: 1.2em;</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="style">font-size: 1.4em;</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>

        <span class="color003366bld">
          <xsl:choose>
            <xsl:when test="name() = 'issue'"><xsl:value-of select="$issue"/></xsl:when>
            <xsl:when test="name() = 'solution'"><xsl:value-of select="$solution"/></xsl:when>
            <xsl:when test="name() = 'cause'"><xsl:value-of select="$cause"/></xsl:when>
            <xsl:when test="name() = 'workaround'"><xsl:value-of select="$workaround"/></xsl:when>
            <xsl:when test="name() = 'summary'"><xsl:value-of select="$summary"/></xsl:when>
          </xsl:choose>
        </span>
      </strong>
      <xsl:text> </xsl:text>
      <xsl:apply-templates mode="concentra_doc"/>
    </div>	                     
	</xsl:template>
	
	<xsl:template match="fix/issue/para | fix/solution/para | fix/workaround/para | fix/summary/para | fix/cause/para" mode="concentra_doc">
    <div style="margin: 1em 0;">
    <xsl:if test="@id">
        <xsl:element name="a">
          <xsl:attribute name="class">udrline</xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="@id"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:if>
      <xsl:apply-templates mode="concentra_doc"/>
    </div>
	</xsl:template>

  <!-- Tables : Begin -->
	
	<xsl:template match="table" mode="concentra_doc">
		<xsl:choose>
			<xsl:when test="@tabstyle='borderless'">
				<table border="0" cellpadding="4" cellspacing="1" width="560" summary="">
				  <xsl:apply-templates mode="concentra_doc"/>
				</table>
			</xsl:when>
			<xsl:otherwise>
				<table border="0" cellpadding="4" cellspacing="1" width="560" summary="" bgcolor="#CCCCCC">
				  <xsl:apply-templates mode="concentra_doc"/>
				</table>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="tabletitle" mode="concentra_doc">
		<caption>
			<strong>
				<xsl:apply-templates mode="concentra_doc"/>
			</strong>
		</caption>
	</xsl:template>
	
	<xsl:template match="thead|tfoot" mode="concentra_doc">
    <xsl:apply-templates mode="concentra_doc"/>
	</xsl:template>
	
	<xsl:template match="tbody" mode="concentra_doc">
		<tbody>
			<xsl:if test="@align">
				<xsl:attribute name="align">
					<xsl:value-of select="@align"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@char">
				<xsl:attribute name="char">
					<xsl:value-of select="@char"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@charoff">
				<xsl:attribute name="charoff">
					<xsl:value-of select="@charoff"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@valign">
				<xsl:attribute name="valign">
					<xsl:value-of select="@valign"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates mode="concentra_doc"/>
		</tbody>
	</xsl:template>
	
	<xsl:template match="thead/row" mode="concentra_doc">
		<tr>
	    <xsl:choose>
	      <xsl:when test="$printable = 'no'">
          <xsl:attribute name="class">theme</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="bgcolor">#E7E7E7</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="concentra_doc"/>
		</tr>
	</xsl:template>
	
	<xsl:template match="thead/row/entry" mode="concentra_doc">
		<th>
			<xsl:if test="@morerows">
				<xsl:attribute name="rowspan">
					<xsl:value-of select="./@morerows + 1"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@nameend">
				<xsl:if test="@namest">
					<xsl:attribute name="colspan">
						<xsl:value-of select="( number(substring(@nameend,4,string-length(@nameend))) - number(substring(@namest,4,string-length(@namest))) ) + 1"/>
					</xsl:attribute>
				</xsl:if>
			</xsl:if>
			<xsl:if test="count(node()) = 0">&#xa0;</xsl:if>
			<xsl:choose>
			  <xsl:when test="$printable = 'no'">
			    <span class="themebody">
			      <xsl:apply-templates mode="concentra_doc"/>
			    </span>
			  </xsl:when>
			  <xsl:otherwise>
			    <xsl:apply-templates mode="concentra_doc"/>
			  </xsl:otherwise>
			</xsl:choose>
		</th>
	</xsl:template>
	
	<xsl:template match="row" mode="concentra_doc">
		<tr>
			<xsl:if test="@align">
				<xsl:attribute name="align">
					<xsl:value-of select="@align"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@char">
				<xsl:attribute name="char">
					<xsl:value-of select="@char"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@charoff">
				<xsl:attribute name="charoff">
					<xsl:value-of select="@charoff"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@valign">
				<xsl:attribute name="valign">
					<xsl:value-of select="@valign"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="../../../@tabstyle = 'borderless'">
					<xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="(position() mod 2) = 0">
							<xsl:attribute name="bgcolor">#E7E7E7</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates mode="concentra_doc"/>
		</tr>
	</xsl:template>

	<xsl:template match="entry" mode="concentra_doc">
		<td>
			<xsl:if test="@align">
				<xsl:attribute name="align">
					<xsl:value-of select="@align"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@valign">
				<xsl:attribute name="valign">
					<xsl:value-of select="@valign"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@morerows">
				<xsl:attribute name="rowspan">
					<xsl:value-of select="./@morerows + 1"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@nameend">
				<xsl:if test="@namest">
					<xsl:attribute name="colspan">
						<xsl:value-of select="( number(substring(@nameend,4,string-length(@nameend))) - number(substring(@namest,4,string-length(@namest))) ) + 1"/>
					</xsl:attribute>
				</xsl:if>
			</xsl:if>
			<xsl:if test="count(node()) = 0">&#xa0;</xsl:if>
			<xsl:apply-templates mode="concentra_doc"/>
		</td>
	</xsl:template>
	
	<xsl:template match="entry/para" mode="concentra_doc">
		<xsl:if test="@id">
      <span>
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </span>
    </xsl:if>
		<xsl:if test="count(node()) = 0">
			<xsl:if test="string-length(.) = 0">&#xa0;</xsl:if>
		</xsl:if>
    <xsl:apply-templates mode="concentra_doc"/>
    <xsl:if test="not(position() = last())"><br/><br/></xsl:if>
	</xsl:template>
	<!-- Tables : End -->
 
	<xsl:template match="hidden.text" mode="concentra_doc">
		<xsl:comment>
      <xsl:apply-templates mode="concentra_doc"/>
		</xsl:comment>
	</xsl:template>
	
  <!-- FORMATTING INFO -->
	
	<xsl:template match="emphasis" mode="concentra_doc">
		<xsl:choose>
			<xsl:when test="@style='bold'">
 				<xsl:if test="not(normalize-space(.) = '')">
   				<strong><xsl:value-of select="."/></strong>
        </xsl:if>
			</xsl:when>
			<xsl:when test="@style='italic'">
				<em><xsl:value-of select="."/></em>
			</xsl:when>
			<xsl:when test="@style='bold-italic'">
				<strong><em><xsl:value-of select="."/></em></strong>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Pop up graphics : Begin -->
	<xsl:template match="pop.up.graphic" mode="concentra_doc">
		<xsl:apply-templates mode="concentra_doc"/>
	</xsl:template>
	
	<xsl:template match="pop.up.graphic/thumbnail/graphic/image" mode="concentra_doc">
		<xsl:variable name="language">
      <xsl:choose>
        <xsl:when test="ancestor::c_support_doc"><xsl:value-of select="ancestor::c_support_doc/@language_code"/></xsl:when>
        <xsl:when test="ancestor::c_hho_marketing_doc"><xsl:value-of select="ancestor::c_hho_marketing_doc/@language_code"/></xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="image_path">
      <xsl:choose>
        <xsl:when test="ancestor::c_hho_marketing_doc"><xsl:value-of select="$marketing_image_path"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$support_image_path"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="zoom_image_name">
      <xsl:choose>
        <xsl:when test="$lc = 'ko'">ico_zoom_ko_kr.gif</xsl:when>
        <xsl:when test="$lc = 'ja'">ico_zoom_ja_jp.gif</xsl:when>
        <xsl:when test="$lc = 'zh-hans'">ico_zoom_zh_cn.gif</xsl:when>
        <xsl:when test="$lc = 'zh-hant'">ico_zoom_zh_tw.gif</xsl:when>
        <xsl:otherwise>ico_zoom.gif</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
		<table>
			<tr>
				<td>
					<xsl:call-template name="render_image"/>
					<br/>
					<xsl:element name="a">
						<xsl:attribute name="href">javascript:popup('<xsl:value-of select="$image_path"/><xsl:value-of select="../../../pop.up/graphic/image/@src"/>','<xsl:value-of select="$language"/>',562,480)</xsl:attribute>
						<div align="right">
							<img src="{$image_path}{$zoom_image_name}" border="0"/>
						</div>
					</xsl:element>
				</td>
			</tr>
		</table>
		<xsl:apply-templates mode="concentra_doc"/>
	</xsl:template>
	
	<xsl:template match="pop.up.graphic/pop.up/graphic" mode="concentra_doc"/>
	<!-- Pop up graphics : End -->
	
	
  <xsl:template match="text()" mode="concentra_doc">
    <!-- [<xsl:value-of select="." disable-output-escaping="no"/>] -->
    <!--
    <xsl:choose>
      <xsl:when test="contains(. ,'»')"><xsl:value-of select="substring-before(. , '»')"/><xsl:text disable-output-escaping="yes">&amp;raquo;</xsl:text><xsl:value-of select="substring-after(. , '»')"/></xsl:when>
      <xsl:when test="contains(. ,'©')"><xsl:value-of select="substring-before(. , '©')"/><xsl:text disable-output-escaping="yes">&amp;copy;</xsl:text><xsl:value-of select="substring-after(. , '©')"/></xsl:when>
      <xsl:when test="contains(. ,'®')"><xsl:value-of select="substring-before(. , '®')"/><xsl:text disable-output-escaping="yes">&amp;reg;</xsl:text><xsl:value-of select="substring-after(. , '®')"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
    </xsl:choose>
  -->
    <xsl:choose>
      <xsl:when test="$lc = 'ja' or $lc = 'ko' or $lc = 'zh-hant' or $lc = 'zh-hans'">
        <xsl:choose>
          <xsl:when test="contains(. , '»')">
            <xsl:call-template name="search-and-replace-entities">
              <xsl:with-param name="input" select="."/>
              <xsl:with-param name="search-string">»</xsl:with-param>
              <xsl:with-param name="replace-string">&amp;raquo;</xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="contains(. , '©')">
            <xsl:call-template name="search-and-replace-entities">
              <xsl:with-param name="input" select="."/>
              <xsl:with-param name="search-string">©</xsl:with-param>
              <xsl:with-param name="replace-string">&amp;copy;</xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="contains(. , '®')">
            <xsl:call-template name="search-and-replace-entities">
              <xsl:with-param name="input" select="."/>
              <xsl:with-param name="search-string">®</xsl:with-param>
              <xsl:with-param name="replace-string">&amp;reg;</xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="." disable-output-escaping="yes"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="." disable-output-escaping="yes"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <xsl:template name="search-and-replace-entities">
    <xsl:param name="input"/>
    <xsl:param name="search-string"/>
    <xsl:param name="replace-string"/>
    <xsl:choose>
      <xsl:when test="$search-string and contains($input, $search-string)">
        <xsl:value-of select="substring-before($input, $search-string)"/>
        <xsl:value-of select="$replace-string" disable-output-escaping="yes"/>
        <xsl:call-template name="search-and-replace-entities">
          <xsl:with-param name="input" select="substring-after($input, $search-string)"/>
          <xsl:with-param name="search-string" select="$search-string"/>
          <xsl:with-param name="replace-string" select="$replace-string"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$input"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


<!-- **********************************************
       Rendering Advisory Content
       
       This was a quick afernoon's worth of work,
       but it may be needed at some point in the 
       future.  This is for handling the special
       advisory content currently being delivered
       to BSC and ITRC
     ********************************************** -->
     
<!-- 
        <xsl:template match="advisory" mode="concentra_doc">
          <strong>Support Communication - Customer Advisory</strong>
          <p>
            <b>Document ID:</b> <xsl:value-of select="ancestor-or-self::c_support_doc/properties/object_name"/><br />
            <b>Version: </b> <xsl:value-of select="ancestor-or-self::c_support_doc/properties/doc_content_version"/><br />
            <b><xsl:value-of select="ancestor-or-self::c_support_doc/title"/></b>
          </p>
          <p style="border: 1px solid; padding: 3px">
            <b>Notice:</b>The information in this document, including products and software versions, is current as of the 
                          Release Date. This document is subject to change without notice.
          </p>
          <p>
            <b>Release Date:</b> <xsl:value-of select="ancestor-or-self::c_support_doc/properties/content_version_date"/><br/>
            <b>Last Updated:</b> <xsl:value-of select="ancestor-or-self::c_support_doc/properties/content_update_date"/>
          </p>
          <hr />
          <p>
            <h3 style="font-weight: bold">Description</h3>
            <xsl:apply-templates select="content.description" mode="concentra_doc"/>
          </p>
          <p>
            <h3 style="font-weight: bold">Scope</h3>
            <xsl:apply-templates select="scope" mode="cocentra_doc"/>
          </p>
          <p>
            <h3 style="font-weight: bold">Resolution</h3>
            <xsl:apply-templates select="resolution" mode="concentra_doc"/>
          </p>     
          <hr />
          <p>
            <div>
              <b>Hardware platforms Affected:</b><xsl:value-of select="ancestor-or-self::c_support_doc/properties/product_group_names/product_group_name"/>
            </div>
            <div>
              <b>Components Affected: </b><xsl:value-of select="ancestor-or-self::c_support_doc/advisory/components.affected"/>
            </div>
            <div>
              <b>Operating Systems Affected: </b><xsl:choose><xsl:when test="components.affected/section/para/text() = ''">Not Applicable</xsl:when><xsl:otherwise><xsl:value-of select="coponents.affected/section/para/text()"/></xsl:otherwise></xsl:choose>
            </div>
            <div>
              <b>Software Affected: </b><xsl:choose><xsl:when test="components.affected/section/para/text() = ''">Not Applicable</xsl:when><xsl:otherwise><xsl:value-of select="coponents.affected/section/para/text()"/></xsl:otherwise></xsl:choose>
            </div>
            <div>
              <b>Third-party Products Affected: </b><xsl:choose><xsl:when test="third.party.products.affected/section/para/text() = ''">Not Applicable</xsl:when><xsl:otherwise><xsl:value-of select="third.party.products.affected/section/para/text()"/></xsl:otherwise></xsl:choose>
            </div>
             <div>
              <b>Support Communication Cross Reference ID: </b><xsl:choose><xsl:when test="cross.reference/section/para = ''">Not Applicable</xsl:when><xsl:otherwise><xsl:value-of select="cross.reference/section/para/text()"/></xsl:otherwise></xsl:choose>
            </div>
         </p>

        </xsl:template>
        <xsl:template match="properties" mode="concentra_doc"/>
-->     


</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2005. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition></MapperMetaTag>
</metaInformation>
-->