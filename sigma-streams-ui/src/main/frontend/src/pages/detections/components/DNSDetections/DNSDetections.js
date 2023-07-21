import React, { useState, useEffect } from "react";
import { Grid } from "@material-ui/core";

// components
import Widget from "../../../../components/Widget";
import mock from "../../../dashboard/mock";
import Table from "../Table";

// styles
import useStyles from "../../styles";

export default function DNSDetections({ dnsData }) {
  var classes = useStyles();
  var keys=['timestamp', 'topic data'];

  console.log(dnsData);

  return (
    <>
      <Grid item xs={12}>
        <Widget
          title="DNS Data"
          upperTitle
          noBodyPadding
          bodyClass={classes.tableWidget}
        >
          <Table data={dnsData} />
        </Widget>
      </Grid>
    </>
  );
}
