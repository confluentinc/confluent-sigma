import React, { useState, useEffect } from "react";
import { Drawer, IconButton, List } from "@material-ui/core";
import {
  Home as HomeIcon,
  BorderAll as TableIcon,
  ArrowBack as ArrowBackIcon,
  Settings as SettingsIcon,
  BarChart as ChartIcon, 
} from "@material-ui/icons";
import {ReactComponent as KafkaIcon} from "./icons/apache-kafka.svg"
import { useTheme } from "@material-ui/styles";
import withRouter from "../Wrappers/withRouter"
import classNames from "classnames";
import {useLocation} from 'react-router-dom';

// styles
import useStyles from "./styles";

// components
import SidebarLink from "./components/SidebarLink/SidebarLink";

// context
import {
  useLayoutState,
  useLayoutDispatch,
  toggleSidebar,
} from "../../context/LayoutContext";

const structure = [
  { id: 0, label: "Home", link: "/app/dashboard", icon: <HomeIcon /> },
  { id: 1, label: "Sigma Rules", link: "/app/sigmarules", icon: <TableIcon /> },
  { id: 2, label: "Detections", icon: <ChartIcon />,
    children: [
      { label: "DNS", link: "/app/detections" },
      { label: "Firewalls", link: "/app/firewalls" },
    ],
  },
  { id: 3, label: "Processors", link: "/app/processors", icon: <KafkaIcon />},
  { id: 9, type: "divider" },
  { id: 10, label: "Settings", link: "/app/settings", icon: <SettingsIcon /> },
];

function Sidebar() {
  var classes = useStyles();
  var theme = useTheme();
  var location = useLocation();

  // global
  var { isSidebarOpened } = useLayoutState();
  var layoutDispatch = useLayoutDispatch();

  // local
  var [isPermanent, setPermanent] = useState(true);

  useEffect(function() {
    window.addEventListener("resize", handleWindowWidthChange);
    handleWindowWidthChange();
    return function cleanup() {
      window.removeEventListener("resize", handleWindowWidthChange);
    };
  });

  return (
    <Drawer
      variant={isPermanent ? "permanent" : "temporary"}
      className={classNames(classes.drawer, {
        [classes.drawerOpen]: isSidebarOpened,
        [classes.drawerClose]: !isSidebarOpened,
      })}
      classes={{
        paper: classNames({
          [classes.drawerOpen]: isSidebarOpened,
          [classes.drawerClose]: !isSidebarOpened,
        }),
      }}
      open={isSidebarOpened}
    >
      <div className={classes.toolbar} />
      <div className={classes.mobileBackButton}>
        <IconButton onClick={() => toggleSidebar(layoutDispatch)}>
          <ArrowBackIcon
            classes={{
              root: classNames(classes.headerIcon, classes.headerIconCollapse),
            }}
          />
        </IconButton>
      </div>
      <List className={classes.sidebarList}>
        {structure.map(link => (
          <SidebarLink
            key={link.id}
            location={location}
            isSidebarOpened={isSidebarOpened}
            {...link}
          />
        ))}
      </List>
    </Drawer>
  );

  // ##################################################################
  function handleWindowWidthChange() {
    var windowWidth = window.innerWidth;
    var breakpointWidth = theme.breakpoints.values.md;
    var isSmallScreen = windowWidth < breakpointWidth;

    if (isSmallScreen && isPermanent) {
      setPermanent(false);
    } else if (!isSmallScreen && !isPermanent) {
      setPermanent(true);
    }
  }
}

export default withRouter(Sidebar);
