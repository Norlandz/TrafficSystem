<ul id="id_HihesPanel">
  <li>
    <h3> Introduction </h3>
    <ul>
      <li>A 2D traffic map, &amp; you can <strong>move</strong> an UnitObj in the Map from one Place to another (-- the Pathing Algorithm finds the way out). </li>
      <li>(Pretty much like how you control the units in an RTS game.)</li>
    </ul>
  </li>
  <li>
    <h3>Overview (graph)</h3>
    <ul>
      <li><img src="images/TrafficSystem Demo.png" height="300px" alt="TrafficSystem Demo"/> </li>
      <li>
        (Github md is limited to display video -- video is moved to end of file)
        <video controls>
          <source src="images/TrafficSystem 20230609_003641 demo01_CrossBlock_Complex.mp4" type="video/mp4">
        </video>
      </li>
      <li>
        (Github md is limited to display video -- video is moved to end of file)
        <video controls>
          <source src="images/TrafficSystem 20230609_004856 demo02_AlignedCube_ComplexMoving_TrafficLight.mp4" type="video/mp4">
        </video>
      </li>
    </ul>
  </li>
  <li>
    <h3> Prior Clarification </h3>
    <ul>
      <li>This is only an <span class="tgn₰hl-ul-nt hlImpAtten meanCateMs hlLineMs hlUllMs wrapOmnt hlMs manGenBuiltin ntMs" data-timestampnt="05200706_23188"><strong>Experimental</strong></span> project<br class="stpMs manGenBuiltin ntMs"/>
      </li>
    </ul>
  </li>
  <li>
    <h3>How To Use</h3>
    <ul>
      <li>Left click to <strong>select</strong> an UnitObj (for movable &amp; selectable -- controllable ones)</li>
      <li>Right click to <strong>move</strong> the UnitObj to that location </li>
    </ul>
  </li>
  <li>
    <h3> Functionalities &amp; Hotkeys</h3>
    <ul>
      <li>vv
        <ul>
          <li>
            <table>
              <tbody>
                <tr>
                  <th scope="col">Functionality</th>
                  <th scope="col">Hotkey</th>
                  <th scope="col">Description</th>
                  <th scope="col">Activated When</th>
                  <th scope="col">*</th>
                </tr>
                <tr>
                  <th scope="row">select an UnitObj<br class="stpMs manGenBuiltin ntMs"/>
                    (for controllable ones)</th>
                  <td>LButton</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">move an UnitObj <br class="stpMs manGenBuiltin ntMs"/>
                    to Target Location</th>
                  <td>RButton</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">move an UnitObj <br class="stpMs manGenBuiltin ntMs"/>
                    to Multi Scheduled Target Locations</th>
                  <td>+RButton</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">pause Movement <br class="stpMs manGenBuiltin ntMs"/>
                    of Selected UnitObj</th>
                  <td>s</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">resume Movement <br class="stpMs manGenBuiltin ntMs"/>
                    of Selected UnitObj</th>
                  <td>e</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">cancel Movement <br class="stpMs manGenBuiltin ntMs"/>
                    of Selected UnitObj</th>
                  <td>c</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">select all  UnitObj<br class="stpMs manGenBuiltin ntMs"/>
                    (for controllable ones)</th>
                  <td>^a</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">unselect all UnitObj</th>
                  <td>MButton</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">select multiple UnitObj</th>
                  <td>^LButton</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">select on the Main UnitObj</th>
                  <td>!s</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">drag an UnitObj<br class="stpMs manGenBuiltin ntMs"/>
                    (for draggable ones)</th>
                  <td>^!LButton 
                    (drag)</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">clear Debug Shapes</th>
                  <td>!a</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <tr>
                  <th scope="row">&nbsp;</th>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
              </tbody>
            </table>
          </li>
          <li>* annotate
            <ul>
              <li>Ahk Hotkey style Notation
                <ul>
                  <li>^ = Ctrl</li>
                  <li>! = Alt</li>
                  <li>+ = Shift</li>
                  <li>LButton = Left Mouse Click </li>
                </ul>
              </li>
            </ul>
          </li>
        </ul>
      </li>
      <li>vv
        <ul>
          <li>You can send a <code><strong>POST</strong></code><strong> request</strong> to the <code>TrafficController</code> Service in Port 18091, eg:</li>
          <li><code>http://localhost:18091/v0.2.1.0/user/createTrafficLight_demo_AlignedCube?sn_row_Target=2&amp;sn_col_Target=1&amp;timeLength_AllowMove=100&amp;timeLength_StopMove=100&amp;mode_VerticalOrHorizontal=false</code> </li>
          <li>(This in turn sends (delegates) a request (internally) to the <code>TrafficPathing</code>)</li>
          <li>&amp; <b class="cp₰font-weight⡅700 wrapOmnt manGenBuiltin ntMs" data-timestampnt="06090121_07888">creates a <code>TrafficLight</code></b> at the specified location (for the demo)</li>
        </ul>
      </li>
    </ul>
  </li>
  <li>
    <h3> Internal Design &amp; Concept</h3>
    <ul>
      <li>
        <h3> Main Java class </h3>
        <ul>
          <li><code>PathingUtil</code> is the central of the <strong>Pathing</strong> Algorithm
            <ul>
              <li><code>com.redfrog.traffic.pathing.PathingUtil.goTo_TargetLocation(UnitObj, double, double, double, GotoMode)</code><br class="stpMs manGenBuiltin ntMs"/>
                is the main Api to use for <strong>Moving</strong> a (controllable) UnitObj to a Target Location </li>
            </ul>
          </li>
          <li><code>TrafficItemControlService</code> is the central for <strong>creating</strong> the UnitObj (&amp; the Map) (&amp; Demo)</li>
        </ul>
      </li>
      <li>
        <h3> Algorithms </h3>
        <ul>
          <li>
            <table>
              <tbody>
                <tr>
                  <th scope="col">algorithm (function name)</th>
                  <th scope="col">success rate</th>
                  <th scope="col">time complexity</th>
                  <th scope="col">description</th>
                  <th scope="col">logic complexity</th>
                </tr>
                <tr>
                  <th scope="row">(*some trivals)</th>
                  <td>5% (low)<br class="stpMs manGenBuiltin ntMs"/>
                    only works for simple shapes</td>
                  <td><code>O(1)</code></td>
                  <td>&nbsp;</td>
                  <td>1%</td>
                </tr>
                <tr>
                  <th scope="row"><code>get_Points_AtCloserDirection_ByAreaDet</code></th>
                  <td>10%<br class="stpMs manGenBuiltin ntMs"/>
                    only works for simple shapes</td>
                  <td><code>O(1)</code></td>
                  <td>&nbsp;</td>
                  <td>10%</td>
                </tr>
                <tr>
                  <th scope="row"><code>repath_by_WrapVertexSearchAlg<br class="stpMs manGenBuiltin ntMs"/>
                  _by_PermutationAndIntersection</code></th>
                  <td>100%<br class="stpMs manGenBuiltin ntMs"/>
                    works in all cases<br class="stpMs manGenBuiltin ntMs"/>
                    always finds the  path (unless unreachable pratically)<br class="stpMs manGenBuiltin ntMs"/>
                    always finds the shortest path</td>
                  <td><code>~O(n!)</code></td>
                  <td><ul>
                      <li>uses a <strong>Permutation</strong> to brute force all the possible paths
                        <ul>
                          <li>(not just random brute force -- some level of logic is still required)</li>
                        </ul>
                      </li>
                      <li>takes very <strong>Long</strong> time to calculate -- when the shape is complex
                        <ul>
                          <li>(optimization are added, made faster, but still not fast enough)</li>
                        </ul>
                      </li>
                    </ul></td>
                  <td>45%</td>
                </tr>
                <tr>
                  <th scope="row"><code>repath_by_WrapVertexSearchAlg<br class="stpMs manGenBuiltin ntMs"/>
                  _by_DirectionalTangentLine_with_ShortestGotoPoint</code></th>
                  <td>70%<br class="stpMs manGenBuiltin ntMs"/>
                    works in many cases<br class="stpMs manGenBuiltin ntMs"/>
                    sometimes fails to find a path (even such path exist pratically)<br class="stpMs manGenBuiltin ntMs"/>
                    often not finding the shortest path</td>
                  <td><code>~O(n)</code></td>
                  <td>&nbsp;</td>
                  <td>50%</td>
                </tr>
                <tr>
                  <th scope="row"><code>repath_by_WrapVertexSearchAlg<br class="stpMs manGenBuiltin ntMs"/>
                  _by_DirectionalTangentLine_with_Permutation</code></th>
                  <td>95%~100%<br class="stpMs manGenBuiltin ntMs"/>
                    works in most/all cases<br class="stpMs manGenBuiltin ntMs"/>
                    always finds the path (should)<br class="stpMs manGenBuiltin ntMs"/>
                    may not find the shortest path</td>
                  <td><code>~O(n)<br class="stpMs manGenBuiltin ntMs"/>
                    ~O(n^2)<br class="stpMs manGenBuiltin ntMs"/>
                    ~O(n!)</code></td>
                  <td><ul>
                      <li>(permutation is involved, a bit)</li>
                      <li>(lots other nested sub algorithms / mechanisms are added for optimization)</li>
                    </ul></td>
                  <td>65%</td>
                </tr>
                <tr>
                  <th scope="row">&nbsp;</th>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
              </tbody>
            </table>
          </li>
          <li>* the rate / complexity values are just rough estimations, no real math / benchmark</li>
        </ul>
      </li>
    </ul>
  </li>
  <li>
    <h3> Technology Stack</h3>
    <ul>
      <li>JavaFx</li>
      <li>Spring</li>
    </ul>
  </li>
  <li>
    <h3> Design Difficulties &amp; Problems facing </h3>
    <ul>
      <li>Number <strong>Precision</strong>
        <ul>
          <li>double precision problem is a messy problem</li>
          <li>in a Graphical app, precise Math calculations are required
            <ul>
              <li>eg: for the logic of Pathing Algorithm</li>
              <li>eg: when you need to check for Collision</li>
              <li>(tbh, this can apply to any other situations too...)</li>
            </ul>
          </li>
        </ul>
      </li>
      <li>Collide &amp; Stuck &amp; Calc <strong>Hang</strong> &amp; Both Dynamic
        <ul>
          <li>this happened many times (likey a bug)</li>
          <li>I cannot tell where this is coming from:
            <ul>
              <li><strong>inf</strong> loop/recursion; </li>
              <li>some random bug (due to collision / determination of sth); </li>
              <li>or there is no bug, but just the calculation is taking too long;</li>
            </ul>
          </li>
          <li>when there is only one UnitObj moving, the collision &amp; pathing looks fine -- does not seem such bug</li>
          <li>when there are <strong>2</strong> UnitObj <strong>moving</strong> -- especially when one of the UnitObj is doing <strong>unstoppable</strong> moving, the <strong>bug</strong> appears way more often
            <ul>
              <li>(-- if none are doing unstoppable moving -- seems fine)</li>
            </ul>
          </li>
        </ul>
      </li>
      <li>Speed &amp; Frame (&amp; Incremental movements)
        <ul>
          <li><code>AnimationTimer</code> is executing base on <strong>Frame</strong>, not Time. </li>
          <li>This makes the calculation of <strong>Speed</strong> &amp; detection of Collision harder.</li>
        </ul>
      </li>
    </ul>
  </li>
  <li>
    <h3>Q&amp;A</h3>
    <ul>
      <li>Where is the Comment &amp; JavaDoc?
        <ul>
          <li>The comments I had in the Code are hideously long &amp; unreadable. So they are all removed.
            <ul>
              <li>(Hopefully this doesnt break the code, it shouldnt but might remove some actual code accidentally during the process.)</li>
            </ul>
          </li>
        </ul>
      </li>
    </ul>
  </li>
  <li> </li>
</ul>



https://raw.githubusercontent.com/Norlandz/TrafficSystem/blob/main/.github/images/TrafficSystem%2020230609_003641%20demo01_CrossBlock_Complex.mp4

https://raw.githubusercontent.com/Norlandz/TrafficSystem/blob/main/.github/images/TrafficSystem%2020230609_004856%20demo02_AlignedCube_ComplexMoving_TrafficLight.mp4
