import React from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import { StompSessionProvider } from "react-stomp-hooks";

// components
import Header from "./Header";
import Sidebar from "./Sidebar";

// pages
import Dashboard from "../pages/dashboard";
import Typography from "../pages/typography";
import Notifications from "../pages/notifications";
import Maps from "../pages/maps";
import Tables from "../pages/tables";
import Icons from "../pages/icons";
import Charts from "../pages/charts";
import SigmaRules from "../pages/sigmarules";
import SigmaRuleEditor from "../pages/sigmarules/SigmaRuleEditor";
import Settings from "../pages/settings";
import Detections from "../pages/detections";
import Firewalls from "../pages/firewalls";
import Processors from "../pages/processors";

import useStyles from "../components/Layout/styles";
import { useLayoutState } from "../context/LayoutContext";
import classnames from "classnames";

export default function App() {
  var classes = useStyles();
  var layoutState = useLayoutState();

  return (
          <div className={classes.root}>
            <>
            <StompSessionProvider
              url={process.env.REACT_APP_SERVER_ENDPOINT + "confluent-sigma-websocket"}
              //All options supported by @stomp/stompjs can be used here
            >
              <Header />
              <Sidebar />
              <div
                className={classnames(classes.content, {
                  [classes.contentShift]: layoutState.isSidebarOpened,
                })}
              >
              <div className={classes.fakeToolbar} />
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/app/sigmarules" element={<SigmaRules />} />
                <Route path="/app/dashboard" element={<Dashboard />} />
                <Route path="/app/typography" element={<Typography />} />
                <Route path="/app/tables" element={<Tables />} />
                <Route path="/app/notifications" element={<Notifications />} />
                <Route path="/app/sigmarules" element={<SigmaRules />} />
                <Route path="/app/sigmaruleeditor" element={<SigmaRuleEditor />} />
                <Route path="/app/settings" element={<Settings />} />
                <Route path="/app/detections" element={<Detections />} />
                <Route path="/app/firewalls" element={<Firewalls />} />
                <Route path="/app/ui" element={() => <Navigate to="/app/ui/icons" />} />
                <Route path="/app/ui/maps" element={<Maps />} />
                <Route path="/app/ui/icons" element={<Icons />} />
                <Route path="/app/ui/charts" element={<Charts />} />
                <Route path="/app/processors" element={<Processors />} />
              </Routes>
              </div>
            </StompSessionProvider>
            </>
          </div>
  )
}
