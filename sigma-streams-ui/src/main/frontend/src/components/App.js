import React, {Fragment} from "react";
import { HashRouter, Route, Routes, Navigate } from "react-router-dom";

// components
import Header from "./Header";
import Sidebar from "./Sidebar";

// pages
import Error from "../pages/error";
import Login from "../pages/login";
import Dashboard from "../pages/dashboard";
import Layout from "./Layout/Layout"
import { LayoutProvider } from "../context/LayoutContext";
import { UserProvider } from "../context/UserContext";
import { ThemeProvider } from "@material-ui/styles";
import { CssBaseline } from "@material-ui/core";
import Themes from "../themes";
import Typography from "../pages/typography";
import Notifications from "../pages/notifications";
import Maps from "../pages/maps";
import Tables from "../pages/tables";
import Icons from "../pages/icons";
import Charts from "../pages/charts";
import SigmaRules from "../pages/sigmarules";
import SigmaRuleEditor from "../pages/sigmaruleeditor";

import useStyles from "../components/Layout/styles";
import { useLayoutState } from "../context/LayoutContext";
import classnames from "classnames";

export default function App() {
  var classes = useStyles();
  var layoutState = useLayoutState();

  return (
          <div className={classes.root}>
            <>
              <Header />
              <Sidebar />
              <div
                className={classnames(classes.content, {
                  [classes.contentShift]: layoutState.isSidebarOpened,
                })}
              >
              <div className={classes.fakeToolbar} />
              <Routes>
                <Route path="/" element={<SigmaRules />} />
                <Route path="/app/sigmarules" element={<SigmaRules />} />
                <Route path="/app/dashboard" element={<Dashboard />} />
                <Route path="/app/typography" element={<Typography />} />
                <Route path="/app/tables" element={<Tables />} />
                <Route path="/app/notifications" element={<Notifications />} />
                <Route path="/app/sigmarules" element={<SigmaRules />} />
                <Route path="/app/sigmaruleeditor" element={<SigmaRuleEditor />} />
                <Route path="/app/ui" element={() => <Navigate to="/app/ui/icons" />} />
                <Route path="/app/ui/maps" element={<Maps />} />
                <Route path="/app/ui/icons" element={<Icons />} />
                <Route path="/app/ui/charts" element={<Charts />} />
              </Routes>
              </div>
            </>
          </div>
  )
}
