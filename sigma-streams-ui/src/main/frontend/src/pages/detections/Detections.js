import React, { useState, useEffect, useRef } from "react";
import { useTheme } from "@material-ui/styles";
import {
  Grid,
} from "@material-ui/core";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  YAxis,
  XAxis,
  Tooltip,
} from "recharts";
import { useSubscription } from "react-stomp-hooks";

// components
import PageTitle from "../../components/PageTitle/PageTitle";
import Widget from "../../components/Widget";
import { Typography } from "../../components/Wrappers";
import Dot from "../../components/Sidebar/components/Dot";
import DataTable from "../../components/DataTable/DataTable"

// styles
import useStyles from "./styles";

//const mainChartData = getMainChartData();

function createData(time, data) {
  return {
    time,
    data
  }
}

var dnsCounter = 0;
var dnsData = [];
var dnsDetectionCounter = 0;
var dnsDetectionData = [];

export default function Detections(props) {
  var classes = useStyles();
  var theme = useTheme();
  //const [dnsData, setDnsData] = useState([]);
  const [time, setTime] = useState((new Date().toLocaleTimeString()));

  const initializeChartData = () => {
    const initialData = [];
    var currentTime = new Date();
    for (let i = 0; i < 30; i++) {
      var iterTime = currentTime.setSeconds(currentTime.getSeconds() - (i * 1000));
      initialData.unshift({
        time: formatTime(new Date()),
        dnsCounter: 0,
        dnsDetectionCounter: 0
        })
    }
    return initialData; 
  }
  const [mainChartData, setMainChartData] = useState(initializeChartData);

  useSubscription("/topic/dns", (dnsTopic) => {
    const newDNSData = JSON.parse(dnsTopic.body);
    
    newDNSData.forEach(dns => {
      dnsData.unshift(createData(formatTime(new Date()), JSON.stringify(dns)));
    });
    dnsCounter += newDNSData.length;
  });

  useSubscription("/topic/dns-detection", (dnsDetectionTopic) => {
    const newDNSDetectionData = JSON.parse(dnsDetectionTopic.body);
    
    newDNSDetectionData.forEach(detection => {
      dnsDetectionData.unshift(createData(formatTime(new Date()), JSON.stringify(detection)));
    });
    dnsDetectionCounter += newDNSDetectionData.length;
  });

  useEffect(() => {
    const interval = setInterval(() => {
      var currentTime = new Date();
      setTime(currentTime.toLocaleTimeString());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    var tableData = mainChartData;
    tableData.shift();
    console.log("current count:" + dnsCounter);
    tableData.push({
      time: formatTime(new Date()),
      dnsCounter: dnsCounter,
      dnsDetectionCounter: dnsDetectionCounter
    });
    dnsCounter = 0;
    dnsDetectionCounter = 0;
  }, [time]);

  function formatTime(event) {
    return event.getHours() + ":" + ('0' + event.getMinutes()).slice(-2) + ":" + ('0' + event.getSeconds()).slice(-2);
  }

  return (
    <>
      <PageTitle title="Detections" />

      <Grid container spacing={4}>
        <Grid item xs={12}>
            <Widget
              bodyClass={classes.mainChartBody}
              header={
                <div className={classes.mainChartHeader}>
                  <Typography
                    variant="h5"
                    color="text"
                    colorBrightness="secondary"
                  >
                    DNS Detections
                  </Typography>
                  <div className={classes.mainChartHeaderLabels}>
                    <div className={classes.mainChartHeaderLabel}>
                      <Dot color="#808080" />
                      <Typography className={classes.mainChartLegentElement}>
                        Raw Data
                      </Typography>
                    </div>
                    <div className={classes.mainChartHeaderLabel}>
                      <Dot color="#ff0000" />
                      <Typography className={classes.mainChartLegentElement}>
                        Detections
                      </Typography>
                    </div>
                  </div>
                </div>
              }
            >
              <ResponsiveContainer width="100%" minWidth={500} height={350}>
                <AreaChart
                  margin={{ top: 15, right: 30, left: -10, bottom: 20 }}
                  data={mainChartData}
                >
                  <YAxis
                    tick={{ fill: theme.palette.text.hint + "80", fontSize: 12 }}
                    stroke={theme.palette.text.hint + "80"}
                    tickLine={true}
                  />
                  <XAxis
                    tick={{ fill: theme.palette.text.hint + "80", fontSize: 12 }}
                    interval={0}
                    stroke={theme.palette.text.hint + "80"}
                    tickLine={true}
                    dataKey="time"
                    angle={45}
                    dx={15}
                    dy={20}
                  />
                  <Tooltip />
                  <Area type="monotone" dataKey="dnsCounter" stroke="#808080" strokeOpacity={0.2} fillOpacity={0.2} fill="#808080" />
                  <Area type="monotone" dataKey="dnsDetectionCounter" stroke="#ff0000" strokeOpacity={0.2} fillOpacity={0.2} fill="#ff0000" />
                </AreaChart>
              </ResponsiveContainer>
            </Widget>
          </Grid>
          <Grid item xs={12}>
            <DataTable title="DNS Detection Data" topicData={dnsDetectionData}/>
          </Grid>
        </Grid>
        <Grid item xs={12}>
            <DataTable title="DNS Raw Data" topicData={dnsData}/>
          </Grid>
     </>
  );
}


